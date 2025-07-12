package cloud.ohiyou.service.impl;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.service.IMessagePushStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author ohiyou
 * 2025/4/26 16:33
 */
public class ServerChanPushStrategy implements IMessagePushStrategy {

    private final OkHttpClient client;

    public ServerChanPushStrategy(OkHttpClient client) {
        this.client = client;
    }

    /**
     * 推送消息
     *
     * @param title   推送的标题
     * @param message 推送的消息（默认是markdown格式）
     */
    @Override
    public void pushMessage(String title, String message) {
        String token = EnvConfig.get().getServerChan();
        String url   = null;
        try {
            url = "https://sctapi.ftqq.com/" + token + ".send?title=" +
                    URLEncoder.encode(title, "UTF-8") +
                    "&desp=" + URLEncoder.encode(message, String.valueOf(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        Request request = new Request.Builder().url(url).build();

        // try-with-resources 关闭 Response
        try (Response resp = client.newCall(request).execute()) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
