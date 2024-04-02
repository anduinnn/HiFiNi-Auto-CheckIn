package cloud.ohiyou;

/**
 * @author ohiyou
 * @since 2024/3/1 14:19
 */

import cloud.ohiyou.utils.DingTalkUtils;
import cloud.ohiyou.utils.WeChatWorkUtils;
import cloud.ohiyou.vo.CookieSignResult;
import cloud.ohiyou.vo.SignResultVO;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String COOKIE = System.getenv("COOKIE");
    //    private static final String COOKIE = "bbs_sid=7bl67b8516gvcn3qejbcsnvotf; bbs_token=1sslA6_2B2TGqkFbnc8t0HPuvahctVQURp_2B28V13Nx2eSp0oEGkMjNS5x9ZTxHrPzbvXgFzvVdcHrd_2BNU2Ar_2F_2FL63RjCym7mrj&bbs_sid=7bl67b8516gvcn3qejbcsnvotf; bbs_token=1sslA6_2B2TGqkFbnc8t0HPuvahctVQURp_2B28V13Nx2eSp0oEGkMjNS5x9ZTxHrPzbvXgFzvVdcHrd_2BNU2Ar_2F_2FL63RjCym7mrj&bbs_sid=7bl67b8516gvcn3qejbcsnvotf; bbs_token=1sslA6_2B2TGqkFbnc8t0HPuvahctVQURp_2B28V13Nx2eSp0oEGkMjNS5x9ZTxHrPzbvXgFzvVdcHrd_2BNU2Ar_2F_2FL63RjCym7mrj";
    private static final String DINGTALK_WEBHOOK = System.getenv("DINGTALK_WEBHOOK"); // 钉钉机器人 access_token 的值
    private static final String WXWORK_WEBHOOK = System.getenv("WXWORK_WEBHOOK"); // 企业微信机器人 key 的值
    private static final String SERVER_CHAN_KEY = System.getenv("SERVER_CHAN");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public static void main(String[] args) {
        List<CookieSignResult> results = new ArrayList<>();
        if (COOKIE == null) {
            throw new RuntimeException("未设置Cookie");
        }

        String[] cookiesArray = COOKIE.split("&");
        log("检测到 " + cookiesArray.length + " 个cookie");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < cookiesArray.length; i++) {
            final String cookie = cookiesArray[i];
            final int index = i;
            executor.submit(() -> {
                try {
                    processCookie(cookie, index, results);
                } catch (Exception e) {
                    log("Error processing cookie at index " + index + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }


        client.dispatcher().executorService().shutdownNow();
        client.connectionPool().evictAll();

        publishResults(results);
    }

    private static void processCookie(String cookie, int index, List<CookieSignResult> results) {
        long startTime = System.currentTimeMillis();
        String formattedCookie = formatCookie(cookie.trim(), index);
        if (formattedCookie != null) {
            SignResultVO signResultVO = sendSignInRequest(formattedCookie);
            long endTime = System.currentTimeMillis();
            results.add(new CookieSignResult(signResultVO, endTime - startTime));
        }
    }

    private static void publishResults(List<CookieSignResult> results) {
        StringBuilder messageBuilder = new StringBuilder();
        boolean allSuccess = true; // 假设所有签到都成功，直到发现失败的签到

        for (CookieSignResult result : results) {
            messageBuilder.append(result.getSignResult().getUserName()).append(": ")
                    .append("\n签到结果: ").append(result.getSignResult().getMessage())
                    .append("\n耗时: ").append(result.getDuration()).append("ms\n\n");
            // 检查每个签到结果，如果有失败的，则设置allSuccess为false
            if (!result.getSignResult().getMessage().contains("成功签到")) {
                allSuccess = false;
            }
        }

        String title = allSuccess ? "签到成功" : "签到失败"; // 根据所有签到结果决定标题
        // 推送
        publishWechat(SERVER_CHAN_KEY, title,messageBuilder.toString());
        DingTalkUtils.pushBotMessage(DINGTALK_WEBHOOK, messageBuilder.toString(), "", "markdown");
        WeChatWorkUtils.pushBotMessage(WXWORK_WEBHOOK, messageBuilder.toString(), "markdown");

    }



    private static SignResultVO sendSignInRequest(String cookie) {
        // 获取签到页面
        String signPageCode = getSignPage(cookie);
        String sign = getSign(signPageCode);
        String userName = getUserName(signPageCode);
        // 获取加密参数
//        String dynamicKey = HiFiNiEncryptUtil.generateDynamicKey();
//        String encryptedSign = HiFiNiEncryptUtil.simpleEncrypt(sign, dynamicKey);

        RequestBody formBody = new FormBody.Builder()
                .add("sign", sign)
                .build();
        // 发送签到请求
        Request request = new Request.Builder()
                .url("https://www.hifini.com/sg_sign.htm")
                .post(formBody)
                .addHeader("Cookie", cookie)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            ;
            String result = readResponse(response);
            SignResultVO signResultVO = stringToObject(result, SignResultVO.class);
            signResultVO.setUserName(userName);
            return signResultVO;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
        return null;
    }

    private static String getUserName(String signPageCode) {
        String userName = "未知";

        // 使用正则匹配用户名
        Pattern pattern = Pattern.compile("<li class=\"nav-item username\"><a class=\"nav-link\" href=\"my.htm\"><img class=\"avatar-1\" src=\".*?\"> (.*?)</a></li>");
        Matcher matcher = pattern.matcher(signPageCode);

        if (matcher.find()) {
            userName = matcher.group(1);
        }
        return userName;
    }

    private static String getSign(String signPageCode) {
        // 通过正则获取sign的值
        if (signPageCode.contains("请登录")) {
            throw new RuntimeException("cookie失效");
        }

        // 使用正则表达式匹配sign变量的值
        Pattern pattern = Pattern.compile("var sign = \"([^\"]+)\"");
        Matcher matcher = pattern.matcher(signPageCode);

        if (matcher.find()) {
            // 如果找到了匹配，提取第一组（括号内的部分）
            String signValue = matcher.group(1);
            System.out.println("Sign的值是: " + signValue);
            return signValue;
        } else {
            throw new RuntimeException("未能获取sign,请检查cookie是否失效");
        }
    }

    /**
     * 获取加密的key
     *
     * @param cookie cookie
     * @return String
     */
    private static String getSignPage(String cookie) {
        // 先携带cookie访问一次签到页面获取sign
        Request request = new Request.Builder()
                .url("https://www.hifini.com/sg_sign.htm")
                .get()
                .addHeader("Cookie", cookie)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return readResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("未能获取sign,请检查cookie是否失效");
    }

    private static String formatCookie(String cookie, int index) {
        String bbsSid = null;
        String bbsToken = null;

        // 分割cookie字符串
        String[] split = cookie.split(";");

        // 遍历分割后的字符串数组
        for (String s : split) {
            s = s.trim(); // 去除可能的前后空格
            // 检查当前字符串是否包含bbs_sid或bbs_token
            if (s.startsWith("bbs_sid=")) {
                bbsSid = s; // 存储bbs_sid
            } else if (s.startsWith("bbs_token=")) {
                bbsToken = s; // 存储bbs_token
            }
        }

        // 确保bbs_sid和bbs_token都不为空
        if (bbsSid != null && bbsToken != null) {
            log("成功解析第 " + (index + 1) + " 个cookie");
            // 拼接bbs_sid和bbs_token并返回
            return bbsSid + ";" + bbsToken + ";";
        } else {
            log("解析第 " + (index + 1) + " 个cookie失败");
            return null; // 或者根据需要抛出异常
        }
    }



    private static void log(String message) {
        System.out.println(message);
    }

    private static String readResponse(Response response) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
        StringBuilder result = new StringBuilder();
        String readLine;
        while ((readLine = reader.readLine()) != null) {
            result.append(readLine);
        }
        return result.toString();
    }


    private static <T> T stringToObject(String result, Class<T> clazz) {
        return JSON.parseObject(result, clazz);
    }

    private static void publishWechat(String serverChanKey, String title, String body) {
        if (serverChanKey == null) {
            log("SERVER_CHAN 环境变量未设置");
            return;
        }

        try {
            String url = "https://sctapi.ftqq.com/" + serverChanKey + ".send?title=" +
                    URLEncoder.encode(title, "UTF-8") + "&desp=" + URLEncoder.encode(body, "UTF-8");
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
