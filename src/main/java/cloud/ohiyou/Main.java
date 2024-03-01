package cloud.ohiyou;

/**
 * @author ohiyou
 * @since 2024/3/1 14:19
 */

import cloud.ohiyou.vo.Cookie;
import cloud.ohiyou.vo.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static final String CLIENT_ID = "null";
    public static final String CAPTCHA_ID = "9464902a3345d323ed58bde565f260ee";
    public static String SI_SIGN = "";
    private static final String COOKIE = System.getenv("COOKIE");
    //    private static final String COOKIE = "bbs_sid=nu7tm5k3o2n6p0572i0ifjfum0; Hm_lvt_4ab5ca5f7f036f4a4747f1836fffe6f2=1702434178,1703638979,1704182270; bbs_token=ergMsAdKvR67sJ4TQWMZCy2SKEcrCBKCnEb3Ouq7eSmjozqO_2FgoUcuojD8EFxLN0yHmZH5NQKlEbdapmYSVPX9jPpIMj0Fpr";
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
            SI_SIGN = getSign();
            if (SI_SIGN == null || SI_SIGN == "") {
//                 cookie过期或者未设置
                if (COOKIE != null) {
                    publishWechat(SERVER_CHAN_KEY, new SignResultVO(-1, "COOKIE 过期,请重新设置"), 0L);
                }
            }
//            CaptchaTaskResultVO resultVO = sendCrackCaptchaRequest(COOKIE);
            SignResultVO signResultVO = sendSignInRequest(COOKIE, null);
            long duration = System.currentTimeMillis() - startTime;

            publishWechat(SERVER_CHAN_KEY, signResultVO, duration);
        } catch (Exception e) {

        }
    }

    private static String getSign() throws IOException {
        Request request = new Request.Builder()
                .url("https://www.hifini.com/")
                .addHeader("Cookie", COOKIE)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String result = readResponse(response);
            // 使用正则表达式匹配所需的值
            Pattern pattern = Pattern.compile("formData.append\\('sg_sign', '(.+?)'\\)");
            Matcher matcher = pattern.matcher(result);

            if (matcher.find()) {
                return matcher.group(1); // 返回匹配到的第一个组（即括号中的部分）
            }

            return result;
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static CaptchaTaskResultVO sendCrackCaptchaRequest(String cookieValue) throws IOException, InterruptedException {
        // 构建参数
        CrackCaptchaTaskVO crackCaptchaTaskVO = new CrackCaptchaTaskVO();
        crackCaptchaTaskVO.setType("GeeTestTaskProxyLess");
        crackCaptchaTaskVO.setWebsiteURL("https://www.hifini.com/");
        crackCaptchaTaskVO.setCaptchaId(CAPTCHA_ID);
        crackCaptchaTaskVO.setProxy("");
        crackCaptchaTaskVO.setGeetestApiServerSubdomain("");

        // 拆分cookie
        String[] split = cookieValue.split(";");
        List<Cookie> cookies = new ArrayList<>();
        for (String item : split) {
            String[] strings = item.split("=");
            Cookie cookie = new Cookie(strings[0].trim(), strings[1].trim());
            cookies.add(cookie);
        }
        crackCaptchaTaskVO.setCookies(cookies);
        CrackCaptchaVO crackCaptchaVO = new CrackCaptchaVO(CLIENT_ID, crackCaptchaTaskVO);

        String json = JSONObject.toJSONString(crackCaptchaVO);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.capsolver.com/createTask")
                .post(body)
                .addHeader("Host", "api.capsolver.com")
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String result = readResponse(response);
            CrackCaptchaResultVO resultVO = stringToObject(result, CrackCaptchaResultVO.class);
            return getTaskResult(resultVO.getTaskId());
        }
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

    private static CaptchaTaskResultVO getTaskResult(String taskId) throws IOException, InterruptedException {
        // 构建参数
        String json = JSON.toJSONString(new CaptchaTaskVO(CLIENT_ID, taskId));

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.capsolver.com/getTaskResult")
                .post(body)
                .addHeader("Host", "api.capsolver.com")
                .addHeader("Content-Type", "application/json")
                .build();

        CaptchaTaskResultVO resultVO = null;
        String status = "processing";
        int maxRetries = 10; // 设置最大重试次数，以避免无限循环
        int retryCount = 0;
        while ((!"ready".equals(status)) && retryCount < maxRetries) {
            try (Response response = client.newCall(request).execute()) {
                String result = readResponse(response);

                // 解析结果
                resultVO = stringToObject(result, CaptchaTaskResultVO.class);
                status = resultVO.getStatus();
            }

            if (!"ready".equals(status)) {
                Thread.sleep(5000); // 设置延迟，比如5秒，以减少服务器负担
                retryCount++;
            }
        }

        if (retryCount >= maxRetries) {
            throw new IOException("已达到最大重试次数，任务仍在处理中。");
        }

        return resultVO;
    }

    private static SignResultVO sendSignInRequest(String cookieValue, CaptchaTaskResultVO resultVO) throws IOException {
//        SolutionVO solutionVO = resultVO.getSolution();

        // 构建参数
//        RequestBody formBody = new FormBody.Builder()
//                .add("lot_number", solutionVO.getLotNumber())
//                .add("captcha_output", solutionVO.getCaptchaOutPut())
//                .add("pass_token", solutionVO.getPassToken())
//                .add("gen_time", solutionVO.getGenTime())
//                .add("sg_sign", SI_SIGN)
//                .build();

        // 构建参数
        RequestBody formBody = new FormBody.Builder()
                .add("lot_number", "ac9a05e6a56f42f7bd48b4d859ae6f26")
                .add("captcha_output", "bLnbSIpbDOqeoY74RN5UKO16sOsEEJ0VtfC1MBeGBtJ_TbJtwNLWpDYVovG190EPZejsPos3w145HMH3gyuZd6iPGy2LjDXQ8lr0OzojLoH752M2ZS3O2fq6w68c1BEJFnlWC4f9gDoLnnbp8aISnVP4Fj5Py3syfHqmS-QgVT_A80ucPwJU5KHOYddUyVr9nn4UP07DHVF8joUi6Vmu2ujQ_UxvXVgIQOaychWjitt2z-txSIZzJqDsrC31Aq6NG8726RCVPZHRoyr65pGZ-_aBLO2_NuRcOrdJSCx2kKL4f0wHl5ytJAMjqWLI-ZHbUNSvqeJelHm89pM2O_m5xPIy_X7RJIs7sh0GDMgHKyOnHxRM4RvETy79_FG1wXphcveCrzdor9u874RPs228gpkIAW2plf4of1CZjwrCe4VBy54vW2F3H4PJJdpp_1PC")
                .add("pass_token", "ec86038f04db5d05e8fcb27353a552aae04981051ca591976d36ac365701e79c")
                .add("gen_time", "1709277249")
                .add("sg_sign", SI_SIGN)
                .build();

        Request request = new Request.Builder()
                .url("https://www.hifini.com/sg_sign.htm")
                .post(formBody)
                .addHeader("Cookie", cookieValue)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String result = readResponse(response);
            return stringToObject(result, SignResultVO.class);
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
