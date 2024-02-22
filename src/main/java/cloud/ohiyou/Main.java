package cloud.ohiyou;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

public class Main {
    public static void main(String[] args) {
        try {
            long startTime = System.nanoTime();

            CloseableHttpClient httpClient = HttpClients.createDefault();

            String cookieValue = System.getenv("COOKIE");
            String serverChanKey = System.getenv("SERVER_CHAN");

            if (cookieValue == null) {
                handleEnvironmentError(httpClient, serverChanKey, new ResultVO(-1, "COOKIE 环境变量未设置"));
                throw new RuntimeException("COOKIE 环境变量未设置");
            }

            HttpResponse response = sendSignInRequest(httpClient, cookieValue);

            long endTime = System.nanoTime();
            Long duration = (endTime - startTime) / 1000000;

            String result = readResponse(response);

            System.out.println(result);
            ResultVO resultVO = toResultVO(result);

            publishWechat(httpClient, serverChanKey, resultVO, duration);

            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleEnvironmentError(CloseableHttpClient httpClient, String serverChanKey, ResultVO resultVO) {
        System.out.println("SERVER_CHAN 环境变量未设置");
        publishWechat(httpClient, serverChanKey, resultVO, null);
    }

    private static HttpResponse sendSignInRequest(CloseableHttpClient httpClient, String cookieValue) throws IOException {
        HttpPost httpPost = new HttpPost("https://www.hifini.com/sg_sign.htm");
        httpPost.setHeader("Cookie", cookieValue);
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");

        return httpClient.execute(httpPost);
    }

    private static String readResponse(HttpResponse response) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String readLine;
        while ((readLine = reader.readLine()) != null) {
            result.append(readLine);
        }
        return result.toString();
    }

    private static ResultVO toResultVO(String result) {
        return JSONObject.parseObject(result, ResultVO.class);
    }

    private static void publishWechat(CloseableHttpClient httpClient, String serverChanKey, ResultVO resultVO, Long duration) {
        if (serverChanKey == null) {
            System.out.println("SERVER_CHAN 环境变量未设置");
            return;
        }

        String title = (resultVO.getMessage().contains("成功签到")) ? "HiFiNi签到成功" : "HiFiNi签到失败";

        if (duration != null) {
            title += "，耗时 " + duration + "ms";
        }

        try {
            HttpGet httpGet = new HttpGet("https://sctapi.ftqq.com/" + serverChanKey + ".send?title=" +
                    URLEncoder.encode(title, "UTF-8") + "&desp=" + URLEncoder.encode(resultVO.getMessage(), "UTF-8"));
            httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}