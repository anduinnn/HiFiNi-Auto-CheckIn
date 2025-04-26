package cloud.ohiyou.service;

import cloud.ohiyou.config.EnvConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.net.URLEncoder;

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
        try {
            String serverChan = EnvConfig.get().getServerChan();
            String url = "https://sctapi.ftqq.com/" + serverChan + ".send?title=" +
                    URLEncoder.encode(title, "UTF-8") + "&desp=" + URLEncoder.encode(message, "UTF-8");
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
