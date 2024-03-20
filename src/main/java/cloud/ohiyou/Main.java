package cloud.ohiyou;

/**
 * @author ohiyou
 * @since 2024/3/1 14:19
 */

import cloud.ohiyou.utils.DingTalkUtils;
import cloud.ohiyou.utils.HiFiNiEncryptUtil;
import cloud.ohiyou.utils.WeChatWorkUtils;
import cloud.ohiyou.vo.SignResultVO;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String COOKIE = System.getenv("COOKIE");
    //    private static final String COOKIE = "bbs_sid=3q22tlil9nsnlh878ajigcdu49; bbs_token=vTOaYxxcCo3L24s_2FFwbDXnDY50kEhyirM7Zs1bvh_2FeQlNq_2B_2FQ2kjlY00b0IOKYhOUaQ3oi0GidfRoJgcnGaFFCzC5NR6LhoT";
    private static final String DINGTALK_WEBHOOK = System.getenv("DINGTALK_WEBHOOK"); // 钉钉机器人 access_token 的值
    private static final String WXWORK_WEBHOOK = System.getenv("WXWORK_WEBHOOK"); // 企业微信机器人 key 的值
    private static final String SERVER_CHAN_KEY = System.getenv("SERVER_CHAN");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public static void main(String[] args) {
        try {
            if (COOKIE == null) {
                log("COOKIE 环境变量未设置");
                return;
            }

            long startTime = System.currentTimeMillis();
            // 处理cookie 格式 只留 bbs_sid 和bbs_token
            String cookie = formatCookie(COOKIE);
            // 发送签到请求
            SignResultVO signResultVO = sendSignInRequest(cookie);
            long endTime = System.currentTimeMillis();

            log("耗时: " + (endTime - startTime) + "ms");
            log("签到结果: " + JSON.toJSONString(signResultVO));

            // 推送
            publishWechat(SERVER_CHAN_KEY, signResultVO, (endTime - startTime));
            DingTalkUtils.pushBotMessage(DINGTALK_WEBHOOK, signResultVO.getMessage(), "", "markdown"); // 推送钉钉机器人
            WeChatWorkUtils.pushBotMessage(WXWork_WEBHOOK, signResultVO.getMessage(), "markdown");
        } catch (Exception e) {
            e.printStackTrace(); // 或者使用日志框架记录异常
        } finally {
            // 关闭 OkHttpClient
            client.dispatcher().executorService().shutdownNow();
            client.connectionPool().evictAll();
        }
    }

    private static SignResultVO sendSignInRequest(String cookie) {
        // 获取Sign
        String sign = getSignKey(cookie);
        // 获取加密参数
        String dynamicKey = HiFiNiEncryptUtil.generateDynamicKey();
        String encryptedSign = HiFiNiEncryptUtil.simpleEncrypt(sign, dynamicKey);

        RequestBody formBody = new FormBody.Builder()
                .add("sign", encryptedSign)
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
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String result = readResponse(response);
            return stringToObject(result, SignResultVO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(encryptedSign);
        return null;
    }

    /**
     * 获取加密的key
     *
     * @param cookie cookie
     * @return String
     */
    private static String getSignKey(String cookie) {
        // 先携带cookie访问一次签到页面获取sign
        Request request = new Request.Builder()
                .url("https://www.hifini.com/sg_sign.htm")
                .get()
                .addHeader("Cookie", cookie)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String result = readResponse(response);

            // 通过正则获取sign的值
            if (result.contains("请登录")) {
                throw new RuntimeException("cookie失效");
            }

            // 使用正则表达式匹配sign变量的值
            Pattern pattern = Pattern.compile("var sign = \"([^\"]+)\"");
            Matcher matcher = pattern.matcher(result);

            if (matcher.find()) {
                // 如果找到了匹配，提取第一组（括号内的部分）
                String signValue = matcher.group(1);
                System.out.println("Sign的值是: " + signValue);
                return signValue;
            } else {
                throw new RuntimeException("未能获取sign,请检查cookie是否失效");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("未能获取sign,请检查cookie是否失效");
    }

    private static String formatCookie(String cookie) {
        String bbsSid = null;
        String bbsToken = null;

        // 分割cookie字符串
        String[] split = cookie.split(";");

        // 遍历分割后的字符串数组
        for (String s : split) {
            s = s.trim(); // 去除可能的前后空格
            // 检查当前字符串是否包含bbs_sid或bbs_token
            if (s.contains("bbs_sid")) {
                bbsSid = s; // 存储bbs_sid
            } else if (s.contains("bbs_token")) {
                bbsToken = s; // 存储bbs_token
            }
        }

        // 确保bbs_sid和bbs_token都不为空
        if (bbsSid != null && bbsToken != null) {
            // 拼接bbs_sid和bbs_token并返回
            return bbsSid + ";" + bbsToken + ";";
        } else {
            throw new RuntimeException("未能解析cookie");
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

    private static void publishWechat(String serverChanKey, SignResultVO signResultVO, Long duration) {
        if (serverChanKey == null) {
            System.out.println("SERVER_CHAN 环境变量未设置");
            return;
        }

        String title = (signResultVO.getMessage().contains("成功签到")) ? "HiFiNi签到成功" : "HiFiNi签到失败";

        if (duration != null) {
            title += "，耗时 " + duration + "ms";
        }

        try {
            String url = "https://sctapi.ftqq.com/" + serverChanKey + ".send?title=" +
                    URLEncoder.encode(title, "UTF-8") + "&desp=" + URLEncoder.encode(signResultVO.getMessage(), "UTF-8");
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
