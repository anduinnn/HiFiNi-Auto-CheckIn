//package cloud.ohiyou;
//
//import cloud.ohiyou.vo.SignResultVO;
//import okhttp3.MediaType;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//import java.io.IOException;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * 废弃的代码,先保留,以后可能会用到
// */
//public class ObsoleteCode {
//    private static final String COOKIE = "";
//
//
//    /**
//     * 获取签到的signKey
//     *
//     * @param result
//     * @return
//     * @throws IOException
//     */
//    private static String getSignKey(String result) throws IOException {
//        String baseUrl = "https://www.hifini.com";
//        // 获取 src 后的地址
//        Pattern patternSrc = Pattern.compile("src=\"([^\"]+)\"");
//        Matcher matcherSrc = patternSrc.matcher(result);
//        if (matcherSrc.find()) {
//            baseUrl += matcherSrc.group(1);
//
//            // 发送renji请求
//            Request request = new Request.Builder()
//                    .url(baseUrl)
//                    .addHeader("Cookie", COOKIE)
//                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)")
//                    .build();
//
//            try (Response response = client.newCall(request).execute()) {
//                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//                // 定义正则表达式模式
//                Pattern pattern = Pattern.compile("/renji_[a-f0-9]+_([a-f0-9]+)\\.js");
//                Matcher matcher = pattern.matcher(result);
//                if (matcher.find()) {
//                    return matcher.group(1);
//                } else {
//                    throw new RuntimeException("未能通过人机校验");
//                }
//            }
//        } else {
//            throw new RuntimeException("未能通过人机校验");
//        }
//    }
//
//    /**
//     * 发送签到请求
//     *
//     * @param cookieValue
//     * @param attempt
//     * @param maxAttempts
//     * @return
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    private static SignResultVO sendSignInRequest(String cookieValue, int attempt, int maxAttempts) throws IOException, InterruptedException {
//        if (attempt > maxAttempts) {
//            System.out.println("已达到最大尝试次数。正在停止执行。");
//            return null;
//        }
//
//        System.out.println("尝试第" + attempt + "次;最大:" + maxAttempts + "次");
//
//        Request request = new Request.Builder()
//                .url("https://www.hifini.com/sg_sign.htm")
//                .post(RequestBody.create("", MediaType.get("application/json; charset=utf-8")))
//                .addHeader("Cookie", cookieValue)
//                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
//                .addHeader("X-Requested-With", "XMLHttpRequest")
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//            String result = readResponse(response);
//            if (result.contains("正在进行人机识别")) {
//                System.out.println("遇到CAPTCHA，正在尝试绕过。");
//
//                String key = getSignKey(result);
//                String token = getRenjiToken(key);
//                cookieValue = formatCookie(cookieValue) + " " + token;
//
//                Thread.sleep(2000);
//                return sendSignInRequest(cookieValue, attempt + 1, maxAttempts);
//            }
//
//            return stringToObject(result, SignResultVO.class);
//        }
//    }
//
//
//    /**
//     * 发送签到请求,最大五次尝试
//     *
//     * @param cookieValue cookieValue
//     * @return
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    private static SignResultVO initialSendSignInRequest(String cookieValue) throws IOException, InterruptedException {
//        return sendSignInRequest(cookieValue, 1, 5);
//    }
//
//    private static String getRenjiToken(String key) throws IOException {
//        // MD5加密的字符串:renji
//        String baseUrl = "https://www.hifini.com/a20be899_96a6_40b2_88ba_32f1f75f1552_yanzheng_ip.php";
//        String type = "96c4e20a0e951f471d32dae103e83881";
//        String value = "05bb5aba7f3f54a677997f862f1a9020";
//
//        // 构建最终的
//        String urlWithParams = String.format("%s?type=%s&key=%s&value=%s", baseUrl, type, key, value);
//
//        Request request = new Request.Builder()
//                .url(urlWithParams)
//                .addHeader("Cookie", COOKIE)
//                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            String header = response.header("Set-Cookie");
//            if (header != null) {
//                String[] split = header.split(";");
//                return split[0];
//            } else {
//                throw new RuntimeException("未能通过人机校验");
//            }
//        }
//    }
//
//}
