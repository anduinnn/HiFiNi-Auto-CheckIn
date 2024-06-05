package cloud.ohiyou.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class TelegramUtils {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();


    /**
     * 微信公众号Service酱推送的key
     * @Author LisonFan
     * @Date&Time 2024/4/28
     * @Migrate 银垚@mtouyao
     * @MigrateDate&Time 2024/6/5
     * @param telegramChatId Telegram Chat ID
     * @param telegramBotToken Telegram Bot Token
     * @param messageText 签到信息
     */
    public static void publishTelegramBot(String telegramChatId, String telegramBotToken, String messageText) {
        if (telegramChatId == null || telegramChatId.isEmpty() || telegramBotToken == null || telegramBotToken.isEmpty()) {
            System.out.println("TG_CHAT_ID 或 TG_BOT_TOKEN 环境变量未设置");
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + telegramBotToken + "/sendMessage?chat_id=" + telegramChatId + "&text=" + URLEncoder.encode(messageText, "UTF-8");
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
