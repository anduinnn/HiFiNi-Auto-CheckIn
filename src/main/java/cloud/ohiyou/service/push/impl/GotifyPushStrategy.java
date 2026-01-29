package cloud.ohiyou.service.push.impl;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.constant.PushPlatform;
import cloud.ohiyou.service.push.AbstractPushStrategy;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Gotify推送策略
 *
 * @author ohiyou
 */
public class GotifyPushStrategy extends AbstractPushStrategy {

    public GotifyPushStrategy(OkHttpClient client) {
        super(client, PushPlatform.GOTIFY);
    }

    @Override
    protected void doPush(String title, String message) throws Exception {
        String gotifyUrl = EnvConfig.get().getGotifyUrl();
        String gotifyAppToken = EnvConfig.get().getGotifyAppToken();

        String url = gotifyUrl + "/message?token=" + gotifyAppToken;

        // 转义换行符
        String bodyStr = "{\"title\": \"" + title + "\", \"message\": \"" + message + "\"}";
        bodyStr = bodyStr.replace("\n", "\\n");

        RequestBody body = RequestBody.create(bodyStr, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = executeRequest(request)) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Gotify响应异常: " + response.code());
            }
        }
    }
}
