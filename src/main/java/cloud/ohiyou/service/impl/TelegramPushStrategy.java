package cloud.ohiyou.service.impl;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.service.IMessagePushStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.net.URLEncoder;

/**
 * @author ohiyou
 * 2025/4/26 16:43
 */
public class TelegramPushStrategy implements IMessagePushStrategy {
    private final OkHttpClient client;

    public TelegramPushStrategy(OkHttpClient client) {
        this.client = client;
    }


    /**
     * @param title   Telegram Bot Token
     * @param message 签到信息
     * @Author LisonFan
     * @Date&Time 2024/4/28
     * @Migrate 银垚@mtouyao
     * @MigrateDate&Time 2024/6/5
     */
    @Override
    public void pushMessage(String title, String message) {
        String telegramChatId = EnvConfig.get().getTgChatId();
        String telegramBotToken = EnvConfig.get().getTgBotToken();

        try {
            String url = "https://api.telegram.org/bot" + telegramBotToken + "/sendMessage?chat_id=" + telegramChatId + "&text=" + URLEncoder.encode(message, "UTF-8");
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
