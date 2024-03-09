package cloud.ohiyou;

/**
 * @author ohiyou
 * @since 2024/3/1 14:19
 */

import cloud.ohiyou.utils.DingTalkUtils;
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
//    private static final String COOKIE = System.getenv("COOKIE");
private static final String COOKIE = "bbs_sid=3q22tlil9nsnlh878a1jigcdu49; bbs_token=fYBfBp5Dh6ghQrBf123123123vdXgiiZiLzIuczjOU2j3ImDluhns2cMDotU7rAyh_2F_2BipT3Q5535SlmXY2l44wsYxbF8MVJucAutQYo3y; 9fd0de187176ef140a5decad986b01c4=d73dd716af5fe8a226715a94aa055390";
    private static final String DINGTALK_WEBHOOK = System.getenv("DINGTALK_WEBHOOK"); // 钉钉机器人 access_token 的值
    private static final String WXWork_WEBHOOK = System.getenv("WXWork_WEBHOOK"); // 企业微信机器人 key 的值
    // private static final String COOKIE = "";
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
            SignResultVO signResultVO = initialSendSignInRequest(cookie);
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
            // 如果任一为空，则返回空字符串或抛出异常，根据具体需求而定
            return "";
        }
    }

    private static String getSignKey(String result) throws IOException {
        String baseUrl = "https://www.hifini.com";
        // 获取 src 后的地址
        Pattern patternSrc = Pattern.compile("src=\"([^\"]+)\"");
        Matcher matcherSrc = patternSrc.matcher(result);
        if (matcherSrc.find()) {
            baseUrl += matcherSrc.group(1);

            // 发送renji请求
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .addHeader("Cookie",COOKIE)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // 定义正则表达式模式
                Pattern pattern = Pattern.compile("/renji_[a-f0-9]+_([a-f0-9]+)\\.js");
                Matcher matcher = pattern.matcher(result);
                if (matcher.find()) {
                    return matcher.group(1);
                } else {
                    throw new RuntimeException("未能通过人机校验");
                }
            }
        } else {
            throw new RuntimeException("未能通过人机校验");
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

    private static SignResultVO sendSignInRequest(String cookieValue, int attempt, int maxAttempts) throws IOException, InterruptedException {
        if (attempt > maxAttempts) {
            System.out.println("已达到最大尝试次数。正在停止执行。");
            return null;
        }

        System.out.println("尝试第" + attempt + "次;最大:" + maxAttempts+"次");

        Request request = new Request.Builder()
                .url("https://www.hifini.com/sg_sign.htm")
                .post(RequestBody.create("", MediaType.get("application/json; charset=utf-8")))
                .addHeader("Cookie", cookieValue)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String result = readResponse(response);
            if (result.contains("正在进行人机识别")) {
                System.out.println("遇到CAPTCHA，正在尝试绕过。");

                String key = getSignKey(result);
                String token = getRenjiToken(key);
                cookieValue = formatCookie(cookieValue) + " " + token;

                Thread.sleep(2000);
                return sendSignInRequest(cookieValue, attempt + 1, maxAttempts);
            }

            return stringToObject(result, SignResultVO.class);
        }
    }

    /**
     * 发送签到请求,最大五次尝试
     * @param cookieValue cookieValue
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static SignResultVO initialSendSignInRequest(String cookieValue) throws IOException, InterruptedException {
        return sendSignInRequest(cookieValue, 1, 5);
    }
    private static String getRenjiToken(String key) throws IOException {
        // MD5加密的字符串:renji
        String baseUrl = "https://www.hifini.com/a20be899_96a6_40b2_88ba_32f1f75f1552_yanzheng_ip.php";
        String type = "96c4e20a0e951f471d32dae103e83881";
        String value = "05bb5aba7f3f54a677997f862f1a9020";

        // 构建最终的
        String urlWithParams = String.format("%s?type=%s&key=%s&value=%s", baseUrl, type, key, value);

        Request request = new Request.Builder()
                .url(urlWithParams)
                .addHeader("Cookie",COOKIE)
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String header = response.header("Set-Cookie");
            if (header != null) {
                String[] split = header.split(";");
                return split[0];
            } else {
                throw new RuntimeException("未能通过人机校验");
            }
        }
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
