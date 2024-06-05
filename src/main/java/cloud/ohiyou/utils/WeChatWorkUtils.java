package cloud.ohiyou.utils;


import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信工具箱
 * @Author 银垚@mtouyao
 * @Date&Time 2024/3/7
 */
public class WeChatWorkUtils {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    /**
     * 企业微信工具箱
     * @Author 银垚@mtouyao
     * @Date&Time 2024/3/7
     * @param WXWorkRobotKey 微信的机器人应用的 key 的值
     * @param messageTitle 签到信息标题
     * @param messageText 签到信息
     * @param msgType (必选其一)text/markdown
     */
    public static void pushBotMessage(String WXWorkRobotKey, String messageTitle, String messageText, String msgType){

        if (WXWorkRobotKey == null || "".equals(WXWorkRobotKey)) {
            System.out.println("WXWORK_WEBHOOK 环境变量未设置");
            return;
        }
        if (msgType == null) {msgType="markdown";}
        // 构建参数
        String jsonBody = "";
        switch (msgType) {
            // 企业微信发送类型详解官网: https://developer.work.weixin.qq.com/document/path/91770
            // 发送text消息
            case "text":
                jsonBody = "{\"msgtype\": \"text\",\"text\": {\"content\":\"HiFiNiBot签到消息通知："+messageTitle+messageText+"\"}}";
                //定义文本内容
                break;
            //发送markdown消息
            case "markdown":
                jsonBody = "{\"msgtype\": \"markdown\",\"markdown\": {\"content\":\"# HiFiNiBot签到消息通知 \n ## "+messageTitle+" \n"+messageText+"\"}}";
                // 定义markdown内容
                break;

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

        // 设置请求body
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        // 设置请求信息
        Request request = new Request.Builder()
                .url("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key="+WXWorkRobotKey)
                .post(body)
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            System.out.println("企业微信机器人返回信息："+response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 微信公众号Service酱推送的key
     * @Author anduinnn
     * @Date&Time
     * @Migrate 银垚@mtouyao
     * @MigrateDate&Time 2024/6/5
     * @param serverChanKey Service酱推送的key
     * @param messageTitle 签到信息标题
     * @param messageBody 签到信息
     */
    public static void pushWechatServiceChan(String serverChanKey, String messageTitle, String messageBody) {
        if (serverChanKey == null || serverChanKey.isEmpty()) {
            System.out.println("SERVER_CHAN 环境变量未设置");
            return;
        }

        try {
            String url = "https://sctapi.ftqq.com/" + serverChanKey + ".send?title=" +
                    URLEncoder.encode(messageTitle, "UTF-8") + "&desp=" + URLEncoder.encode(messageBody, "UTF-8");
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
