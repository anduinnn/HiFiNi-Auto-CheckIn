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
 * 企业微信机器人推送策略
 *
 * @author ohiyou
 */
public class WeChatWorkPushStrategy extends AbstractPushStrategy {

    private static final String API_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=";

    public WeChatWorkPushStrategy(OkHttpClient client) {
        super(client, PushPlatform.WECHAT_WORK);
    }

    @Override
    protected void doPush(String title, String message) throws Exception {
        String wxWorkRobotKey = EnvConfig.get().getWxworkrobotkey();
        String messageType = EnvConfig.get().getWXWorkMessageType();

        String jsonBody = buildMessageJson(messageType, title, message);
        if (jsonBody == null) {
            logger.warn("不支持的消息类型: {}", messageType);
            return;
        }

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL + wxWorkRobotKey)
                .post(body)
                .build();

        try (Response response = executeRequest(request)) {
            if (response.body() != null) {
                logger.debug("企业微信机器人返回信息: {}", response.body().string());
            }
            if (!response.isSuccessful()) {
                throw new RuntimeException("企业微信响应异常: " + response.code());
            }
        }
    }

    /**
     * 构建消息体
     */
    private String buildMessageJson(String msgType, String title, String message) {
        switch (msgType) {
            case "text":
                return "{\"msgtype\": \"text\",\"text\": {\"content\":\"HiFiNiBot签到消息通知：" + title + message + "\"}}";
            case "markdown":
                return "{\"msgtype\": \"markdown\",\"markdown\": {\"content\":\"# HiFiNiBot签到消息通知 \n ## " + title + " \n" + message + "\"}}";
            case "image":
                logger.warn("[企业微信机器人]暂未开通image类型消息!");
                return null;
            case "news":
                logger.warn("[企业微信机器人]暂未开通news类型消息!");
                return null;
            case "file":
                logger.warn("[企业微信机器人]暂未开通file类型消息!");
                return null;
            case "voice":
                logger.warn("[企业微信机器人]暂未开通voice类型消息!");
                return null;
            default:
                logger.warn("[企业微信机器人]消息类型未匹配! msgType={}", msgType);
                return null;
        }
    }
}
