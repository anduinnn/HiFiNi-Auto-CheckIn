package cloud.ohiyou;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.factory.PushStrategyFactory;
import cloud.ohiyou.service.IMessagePushStrategy;
import cloud.ohiyou.utils.OkHttpUtils;
import cloud.ohiyou.vo.CookieSignResult;
import cloud.ohiyou.vo.SignResultVO;
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

public class HifiniMain {
    private static final OkHttpClient client = OkHttpUtils.getClient();

    public static void main(String[] args) {
        List<CookieSignResult> results = Collections.synchronizedList(new ArrayList<>());
        String cookies = EnvConfig.get().getCookie();
        if (cookies == null) {
            throw new RuntimeException("未设置Cookie");
        }

        String[] cookiesArray = cookies.split("&");
        log("检测到 " + cookiesArray.length + " 个cookie");

        // 创建一个固定线程数的线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < cookiesArray.length; i++) {
            final String cookie = cookiesArray[i];
            final int index = i;
            Future<?> future = executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    // 格式化cookie,去除掉空格 空字符串等，同时也检测cookie的格式
                    String formattedCookie = formatCookie(cookie.trim(), index);
                    if (formattedCookie != null) {
                        SignResultVO signResultVO = sendSignInRequest(formattedCookie);
                        long endTime = System.currentTimeMillis();
                        results.add(new CookieSignResult(signResultVO, endTime - startTime));
                    }
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
        publishResults(results);

        // 关闭OkHttpClient
        client.dispatcher().executorService().shutdownNow();
        client.connectionPool().evictAll();
    }

    /**
     * 推送签到结果
     *
     * @param results results，每个cookie的签到结果
     */
    private static void publishResults(List<CookieSignResult> results) {
        StringBuilder messageBuilder = new StringBuilder();
        boolean allSuccess = true;

        if (results == null || results.isEmpty()) {
            allSuccess = false;
            messageBuilder.append("未获取到任何签到结果，请检查任务执行情况。\n");
        } else {
            for (CookieSignResult result : results) {
                messageBuilder.append(result.getSignResult().getUserName()).append(": ")
                        .append("\n签到结果: ").append(result.getSignResult().getMessage())
                        .append("\n耗时: ").append(result.getDuration()).append("ms\n\n");
                if (!result.getSignResult().getMessage().contains("成功签到")) {
                    allSuccess = false;
                }
            }
        }

        String title = allSuccess ? "HiFiNi签到成功" : "HiFiNi签到失败";

        System.out.println("\nHiFiNi签到消息: \n" + title + "：\n" + messageBuilder);

        // 推送
        List<IMessagePushStrategy> strategies = PushStrategyFactory.getStrategy();
        for (IMessagePushStrategy strategy : strategies) {
            strategy.pushMessage(title, messageBuilder.toString());
        }
    }


    /**
     * 携带cookie模拟访问网页,执行签到
     *
     * @param cookie cookie
     * @return 签到返回的结果
     */
    private static SignResultVO sendSignInRequest(String cookie) {
        // 获取签到页面
        String signPageCode = getSignPage(cookie);
        String sign = getSign(signPageCode);
        String userName = getUserName(signPageCode);

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

    /**
     * 获取sign值
     *
     * @param signPageCode 网页数据
     * @return String
     */
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
     * 携带cookie，获取网页数据
     *
     * @param cookie cookie
     * @return sign
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

    /**
     * 检测cookie的格式，是否是  bbs_sid,bbs_token,否则cookie是无效的；
     * 去除掉多余的空格,空字符串
     *
     * @param cookie cookie
     * @param index  index
     * @return formatCookie
     */
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
        if (response.body() == null) {
            throw new IOException("Response body is null");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
            StringBuilder result = new StringBuilder();
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                result.append(readLine);
            }
            return result.toString();
        }
    }


    private static <T> T stringToObject(String result, Class<T> clazz) {
        return JSON.parseObject(result, clazz);
    }
}
