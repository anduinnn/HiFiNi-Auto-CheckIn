package cloud.ohiyou.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.request.OapiRobotSendRequest;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信工具箱
 * @Author 银垚@mtouyao
 * @Date&Time 2024/3/7 16:40
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
     * @Date&Time 2024/3/7 16:40
     * @param WXWorkRobotKey 微信的机器人应用的 key 的值
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
                jsonBody = "{\"msgtype\": \"text\",\"text\": {\"content\":\"HiFiNi签到消息通知："+messageTitle+messageText+"\"}}";
                //定义文本内容
                break;
            //发送markdown消息
            case "markdown":
                jsonBody = "{\"msgtype\": \"markdown\",\"markdown\": {\"content\":\"# HiFiNi签到消息通知 \n ## "+messageTitle+" \n"+messageText+"\"}}";
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
}
