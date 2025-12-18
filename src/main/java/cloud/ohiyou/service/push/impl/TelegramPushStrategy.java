package cloud.ohiyou.service.push.impl;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.constant.PushPlatform;
import cloud.ohiyou.service.push.AbstractPushStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Telegram机器人推送策略
 *
 * @author ohiyou
 */
public class TelegramPushStrategy extends AbstractPushStrategy {

    private static final String API_URL = "https://api.telegram.org/bot";

    public TelegramPushStrategy(OkHttpClient client) {
        super(client, PushPlatform.TELEGRAM);
    }

    @Override
    protected void doPush(String title, String message) throws Exception {
        String telegramChatId = EnvConfig.get().getTgChatId();
        String telegramBotToken = EnvConfig.get().getTgBotToken();

        String url = API_URL + telegramBotToken + "/sendMessage?chat_id=" +
                telegramChatId + "&text=" + encodeUrl(title + "\n" + message);

        Request request = new Request.Builder().url(url).build();
        try (Response response = executeRequest(request)) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Telegram响应异常: " + response.code());
            }
        }
    }
}
