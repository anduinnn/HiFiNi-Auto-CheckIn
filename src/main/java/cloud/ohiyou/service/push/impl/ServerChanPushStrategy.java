package cloud.ohiyou.service.push.impl;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.constant.PushPlatform;
import cloud.ohiyou.service.push.AbstractPushStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Server酱推送策略
 *
 * @author ohiyou
 */
public class ServerChanPushStrategy extends AbstractPushStrategy {

    private static final String API_URL = "https://sctapi.ftqq.com/";

    public ServerChanPushStrategy(OkHttpClient client) {
        super(client, PushPlatform.SERVER_CHAN);
    }

    @Override
    protected void doPush(String title, String message) throws Exception {
        String token = EnvConfig.get().getServerChan();
        String url = API_URL + token + ".send?title=" +
                encodeUrl(title) + "&desp=" + encodeUrl(message);

        Request request = new Request.Builder().url(url).build();
        try (Response response = executeRequest(request)) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Server酱响应异常: " + response.code());
            }
        }
    }
}
