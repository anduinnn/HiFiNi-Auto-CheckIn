package cloud.ohiyou.service;

import cloud.ohiyou.config.EnvConfig;
import okhttp3.*;

import java.io.IOException;

/**
 * 企业微信推送策略类
 */
public class WeChatWorkPushStrategy implements IMessagePushStrategy {

    private final OkHttpClient client;

    public WeChatWorkPushStrategy(OkHttpClient client) {
        this.client = client;
    }


    /**
     * 企业微信工具箱
     *
     * @param title   签到信息标题
     * @param message 签到信息
     * @Author 银垚@mtouyao
     * @Date&Time 2024/3/7
     */
    @Override
    public void pushMessage(String title, String message) {

        String wxWorkRobotKey = EnvConfig.get().getWxworkrobotkey();
        String messageType = EnvConfig.get().getWXWorkMessageType();

        String jsonBody = buildMessageJson(messageType, title, message);
        if (jsonBody == null) return;

        // 设置请求body
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        // 设置请求信息
        Request request = new Request.Builder()
                .url("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=" + wxWorkRobotKey)
                .post(body)
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            System.out.println("企业微信机器人返回信息：" + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建消息体
     *
     * @param msgType msgType
     * @param title   title
     * @param message message
     * @return
     */
    private String buildMessageJson(String msgType, String title, String message) {
        switch (msgType) {
            // 企业微信发送类型详解官网: https://developer.work.weixin.qq.com/document/path/91770
            // 发送text消息
            case "text":
                return "{\"msgtype\": \"text\",\"text\": {\"content\":\"HiFiNiBot签到消息通知：" + title + message + "\"}}";
            //发送markdown消息
            case "markdown":
                return "{\"msgtype\": \"markdown\",\"markdown\": {\"content\":\"# HiFiNiBot签到消息通知 \n ## " + title + " \n" + message + "\"}}";
            // 发送image消息（图片）
            case "image":
                System.out.println("[企业微信机器人]暂未开通image类型消息!");
                break;
            // 发送news消息（图文）
            case "news":
                System.out.println("[企业微信机器人]暂未开通news类型消息!");
                break;
            // 发送file消息（文件）
            case "file":
                System.out.println("[企业微信机器人]暂未开通file类型消息!");
                break;
            // 发送voice消息（语音）
            case "voice":
                System.out.println("[企业微信机器人]暂未开通voice类型消息!");
                break;

            /** 未匹配 */
            default:
                System.out.println("[企业微信机器人]消息类型未匹配!请检查调用pushBotMessage传递的msgType参数");
                break;

        }
        return null;
    }
}
