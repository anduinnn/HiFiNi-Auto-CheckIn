package cloud.ohiyou.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GotifyUtils {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * @Author SmileMachine
     * @Date&Time 2025-03-04
     * @param gotifyUrl Gotify URL
     * @param gotifyAppToken Gotify App Token
     * @param messageText 签到信息
     */
    public static void pushGotifyApp(String gotifyUrl, String gotifyAppToken, String messageTitle, String messageText) {
        if (gotifyUrl == null || gotifyUrl.isEmpty() || gotifyAppToken == null || gotifyAppToken.isEmpty()) {
            System.out.println("Gotify URL 或 Gotify App Token 环境变量未设置");
            return;
        }

        try {
            String url = gotifyUrl + "/message?token=" + gotifyAppToken;
            RequestBody body = RequestBody.create(
                "{\"title\": \"" + messageTitle + "\", \"message\": \"" + messageText + "\"}",
                    MediaType.get("application/json; charset=utf-8")
            );
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
