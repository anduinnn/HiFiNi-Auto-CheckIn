package cloud.ohiyou.actions;

/**
 * @author ohiyou
 * @since 2024/3/1 14:19
 */


import cloud.ohiyou.actions.utils.DingTalkUtils;
import cloud.ohiyou.actions.utils.TelegramUtils;
import cloud.ohiyou.actions.utils.WeChatWorkUtils;
import cloud.ohiyou.actions.vo.CookieSignResult;
import cloud.ohiyou.actions.vo.SignResultVO;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    /** ↓↓↓↓↓↓↓↓↓↓ 测试 ↓↓↓↓↓↓↓↓↓↓ */
//    private static final String COOKIE = cloud.ohiyou.test.TestEnum.COOKIE.getValue();
//    private static final String SERVER_CHAN_KEY = cloud.ohiyou.test.TestEnum.SERVER_CHAN_KEY.getValue(); // Service酱推送的key
//    private static final String WXWORK_WEBHOOK = cloud.ohiyou.test.TestEnum.WXWORK_WEBHOOK.getValue(); // 企业微信机器人 key 的值
//    private static final String DINGTALK_WEBHOOK = cloud.ohiyou.test.TestEnum.DINGTALK_WEBHOOK.getValue(); // 钉钉机器人 access_token 的值
//    private static final String TG_CHAT_ID = cloud.ohiyou.test.TestEnum.TG_CHAT_ID.getValue(); // Telegram Chat ID
//    private static final String TG_BOT_TOKEN = cloud.ohiyou.test.TestEnum.TG_BOT_TOKEN.getValue(); // Telegram Bot Token
    /** ↑↑↑↑↑↑↑↑↑↑ 测试 ↑↑↑↑↑↑↑↑↑↑ */

    /** ↓↓↓↓↓↓↓↓↓↓ 正式 ↓↓↓↓↓↓↓↓↓↓ */
    private static final String COOKIE = System.getenv("COOKIE");
    private static final String DINGTALK_WEBHOOK = System.getenv("DINGTALK_WEBHOOK"); // 钉钉机器人 access_token 的值
    private static final String WXWORK_WEBHOOK = System.getenv("WXWORK_WEBHOOK"); // 企业微信机器人 key 的值
    private static final String SERVER_CHAN_KEY = System.getenv("SERVER_CHAN"); // Service酱推送的key
    private static final String TG_CHAT_ID = System.getenv("TG_CHAT_ID"); // Telegram Chat ID
    private static final String TG_BOT_TOKEN = System.getenv("TG_BOT_TOKEN"); // Telegram Bot Token
    /** ↑↑↑↑↑↑↑↑↑↑ 正式 ↑↑↑↑↑↑↑↑↑↑ */
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public static void main(String[] args) {
        List<CookieSignResult> results = Collections.synchronizedList(new ArrayList<>());
        if (COOKIE == null) {
            throw new RuntimeException("未设置Cookie");
        }

        String[] cookiesArray = COOKIE.split("&");
        log("检测到 " + cookiesArray.length + " 个cookie");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < cookiesArray.length; i++) {
            final String cookie = cookiesArray[i];
            final int index = i;
            Future<?> future = executor.submit(() -> {
                try {
                    processCookie(cookie, index, results);
                } catch (Exception e) {
                    log("Error processing cookie at index " + index + ": " + e.getMessage());
                    // 添加消息失败的结果
                    results.add(new CookieSignResult(new SignResultVO(401, "签到失败,cookie失效"), 0));
                }
            });
            futures.add(future);
        }

        // 关闭线程池，使其不再接受新任务
        executor.shutdown();

        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                // 等待并获取任务执行结果,这会阻塞，直到任务完成
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 重新中断当前线程
                // 处理中断异常，例如记录日志或者根据业务需求进行其他处理
                log("当前线程在等待任务完成时被中断。");
            } catch (ExecutionException e) {
                // 获取实际导致任务执行失败的异常
                Throwable cause = e.getCause();
                log("执行任务时出错：" + cause.getMessage());
            }
        }

        // 等待线程池完全终止
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
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

        String title = allSuccess ? "HiFiNi签到成功" : "HiFiNi签到失败"; // 根据所有签到结果决定标题

        System.out.println("\nHiFiNi签到消息: \n" + title + "：\n" + messageBuilder.toString());
        // 推送
        WeChatWorkUtils.pushWechatServiceChan(SERVER_CHAN_KEY, title,messageBuilder.toString()); // 推送微信公众号Service酱
        WeChatWorkUtils.pushBotMessage(WXWORK_WEBHOOK, title, messageBuilder.toString(), "markdown"); // 推送企业微信机器人
        DingTalkUtils.pushBotMessage(DINGTALK_WEBHOOK, title, messageBuilder.toString(), "", "markdown"); // 推送钉钉机器人
        TelegramUtils.publishTelegramBot(TG_CHAT_ID, TG_BOT_TOKEN, "HiFiNi签到消息: \n" + title + "：\n" + messageBuilder.toString()); // push telegram bot
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
}
