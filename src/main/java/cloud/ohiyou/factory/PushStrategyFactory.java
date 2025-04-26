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
        if (StringUtils.isNotBlank(EnvConfig.get().getServerChan())) {
            strategies.add(new ServerChanPushStrategy(client));
        }
        if (StringUtils.isNotBlank(EnvConfig.get().getWxworkrobotkey())) {
            strategies.add(new WeChatWorkPushStrategy(client));
        }
        if (StringUtils.isNotBlank(EnvConfig.get().getDingTalkRobotKey())) {
            strategies.add(new DingTalkPushStrategy());
        }
        if (StringUtils.isNotBlank(EnvConfig.get().getGotifyAppToken()) && StringUtils.isNotBlank(EnvConfig.get().getGotifyUrl())) {
            strategies.add(new GotifyPushStrategy(client));
        }
        if (StringUtils.isNotBlank(EnvConfig.get().getTgBotToken()) && StringUtils.isNotBlank(EnvConfig.get().getTgChatId())) {
            strategies.add(new TelegramPushStrategy(client));
        }
        return strategies;
    }
}
