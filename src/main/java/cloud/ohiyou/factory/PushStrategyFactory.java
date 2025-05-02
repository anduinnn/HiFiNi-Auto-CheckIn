package cloud.ohiyou.factory;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.service.*;
import cloud.ohiyou.utils.OkHttpUtils;
import cloud.ohiyou.utils.StringUtils;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 推送策略工厂类
 *
 * @author Alune
 */
public class PushStrategyFactory {

    public static List<IMessagePushStrategy> getStrategy() {
        List<IMessagePushStrategy> strategies = new ArrayList<>();
        OkHttpClient client = OkHttpUtils.getClient();
        List<String> enablePlatforms = new ArrayList<>();
        if (StringUtils.isNotBlank(EnvConfig.get().getServerChan())) {
            strategies.add(new ServerChanPushStrategy(client));
            enablePlatforms.add("Server酱");
        }
        if (StringUtils.isNotBlank(EnvConfig.get().getWxworkrobotkey())) {
            strategies.add(new WeChatWorkPushStrategy(client));
            enablePlatforms.add("企业微信机器人");
        }
        if (StringUtils.isNotBlank(EnvConfig.get().getDingTalkRobotKey())) {
            strategies.add(new DingTalkPushStrategy());
            enablePlatforms.add("钉钉机器人");
        }
        if (StringUtils.isNotBlank(EnvConfig.get().getGotifyAppToken()) && StringUtils.isNotBlank(EnvConfig.get().getGotifyUrl())) {
            strategies.add(new GotifyPushStrategy(client));
            enablePlatforms.add("Gotify");
        }
        if (StringUtils.isNotBlank(EnvConfig.get().getTgBotToken()) && StringUtils.isNotBlank(EnvConfig.get().getTgChatId())) {
            strategies.add(new TelegramPushStrategy(client));
            enablePlatforms.add("Telegram 机器人");
        }

        // 打印结果
        if (enablePlatforms.isEmpty()) {
            System.out.println("未启用任何推送平台.");
        } else {
            System.out.println("已启用推送平台（共" + enablePlatforms.size() + "个）: " + String.join(", ", enablePlatforms));
        }
        return strategies;
    }
}
