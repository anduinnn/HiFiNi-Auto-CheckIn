package cloud.ohiyou.factory;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.service.push.AbstractPushStrategy;
import cloud.ohiyou.service.push.IMessagePushStrategy;
import cloud.ohiyou.service.push.impl.*;
import cloud.ohiyou.utils.OkHttpUtils;
import cloud.ohiyou.utils.StringUtils;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 推送策略工厂类
 *
 * @author ohiyou
 */
public class PushStrategyFactory {

    private static final Logger logger = LoggerFactory.getLogger(PushStrategyFactory.class);

    private PushStrategyFactory() {
        // 防止实例化
    }

    /**
     * 根据配置创建推送策略列表
     *
     * @return 推送策略列表
     */
    public static List<IMessagePushStrategy> createStrategies() {
        List<IMessagePushStrategy> strategies = new ArrayList<>();
        OkHttpClient client = OkHttpUtils.getClient();
        EnvConfig config = EnvConfig.get();

        // Server酱
        registerIfConfigured(strategies,
                config.getServerChan(),
                () -> new ServerChanPushStrategy(client));

        // 企业微信机器人
        registerIfConfigured(strategies,
                config.getWxworkrobotkey(),
                () -> new WeChatWorkPushStrategy(client));

        // 钉钉机器人
        registerIfConfigured(strategies,
                config.getDingTalkRobotKey(),
                () -> new DingTalkPushStrategy(client));

        // Gotify
        registerIfConfigured(strategies,
                () -> StringUtils.isNotBlank(config.getGotifyUrl())
                        && StringUtils.isNotBlank(config.getGotifyAppToken()),
                () -> new GotifyPushStrategy(client));

        // Telegram 机器人
        registerIfConfigured(strategies,
                () -> StringUtils.isNotBlank(config.getTgBotToken())
                        && StringUtils.isNotBlank(config.getTgChatId()),
                () -> new TelegramPushStrategy(client));

        logEnabledPlatforms(strategies);
        return strategies;
    }

    /**
     * 根据配置注册策略（单个配置项）
     */
    private static void registerIfConfigured(List<IMessagePushStrategy> strategies,
                                              String config,
                                              Supplier<IMessagePushStrategy> factory) {
        if (StringUtils.isNotBlank(config)) {
            strategies.add(factory.get());
        }
    }

    /**
     * 根据配置注册策略（多个配置项条件）
     */
    private static void registerIfConfigured(List<IMessagePushStrategy> strategies,
                                              Supplier<Boolean> condition,
                                              Supplier<IMessagePushStrategy> factory) {
        if (condition.get()) {
            strategies.add(factory.get());
        }
    }

    /**
     * 记录已启用的推送平台
     */
    private static void logEnabledPlatforms(List<IMessagePushStrategy> strategies) {
        if (strategies.isEmpty()) {
            logger.warn("未启用任何推送平台");
        } else {
            String platforms = strategies.stream()
                    .filter(s -> s instanceof AbstractPushStrategy)
                    .map(s -> ((AbstractPushStrategy) s).getPlatform().getDisplayName())
                    .collect(Collectors.joining(", "));
            logger.info("已启用推送平台(共{}个): {}", strategies.size(), platforms);
        }
    }

    /**
     * 获取推送策略列表（兼容旧方法名）
     *
     * @return 推送策略列表
     * @deprecated 使用 {@link #createStrategies()} 代替
     */
    @Deprecated
    public static List<IMessagePushStrategy> getStrategy() {
        return createStrategies();
    }
}
