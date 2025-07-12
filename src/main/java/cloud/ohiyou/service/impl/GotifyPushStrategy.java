package cloud.ohiyou.service.impl;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.service.IMessagePushStrategy;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GotifyPushStrategy implements IMessagePushStrategy {

    private final OkHttpClient client;

    public GotifyPushStrategy(OkHttpClient client) {
        this.client = client;
    }


    /**
     * @param title   title
     * @param message message
     * @Author SmileMachine
     * @Date&Time 2025-03-04
     */
    @Override
    public void pushMessage(String title, String message) {
        String gotifyUrl = EnvConfig.get().getGotifyUrl();
        String gotifyAppToken = EnvConfig.get().getGotifyAppToken();
        try {
            String url = gotifyUrl + "/message?token=" + gotifyAppToken;
            String bodyStr = "{\"title\": \"" + title + "\", \"message\": \"" + message + "\"}";
            bodyStr = bodyStr.replace("\n", "\\n");
            RequestBody body = RequestBody.create(
                    bodyStr,
                    MediaType.get("application/json; charset=utf-8"));
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
