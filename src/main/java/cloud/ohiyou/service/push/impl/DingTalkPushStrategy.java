package cloud.ohiyou.service.push.impl;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.constant.PushPlatform;
import cloud.ohiyou.service.push.AbstractPushStrategy;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import okhttp3.OkHttpClient;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 钉钉机器人推送策略
 *
 * @author ohiyou
 */
public class DingTalkPushStrategy extends AbstractPushStrategy {

    private static final String API_URL = "https://oapi.dingtalk.com/robot/send";

    public DingTalkPushStrategy(OkHttpClient client) {
        super(client, PushPlatform.DINGTALK);
    }

    @Override
    protected void doPush(String title, String message) throws Exception {
        String dingTalkRobotKey = EnvConfig.get().getDingTalkRobotKey();
        String msgType = "markdown";

        // 生成签名
        Long timestamp = System.currentTimeMillis();
        String secret = "messageKey"; // 安全设置，加签密钥
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");

        // 创建钉钉客户端
        DingTalkClient client = new DefaultDingTalkClient(
                API_URL + "?sign=" + sign + "&timestamp=" + timestamp);
        OapiRobotSendRequest req = new OapiRobotSendRequest();

        switch (msgType) {
            case "text":
                OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
                text.setContent("HiFiNiBot签到信息:" + title + message);
                req.setMsgtype("text");
                req.setText(text);
                break;
            case "markdown":
                OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
                markdown.setTitle("HiFiNiBot消息通知");
                markdown.setText("# HiFiNiBot签到消息通知 \n ## " + title + " \n" + message);
                req.setMsgtype("markdown");
                req.setMarkdown(markdown);
                break;
            case "link":
                logger.warn("[钉钉机器人]暂未开通link类型消息!");
                return;
            case "ActionCard":
                logger.warn("[钉钉机器人]暂未开通ActionCard类型消息!");
                return;
            case "FeedCard":
                logger.warn("[钉钉机器人]暂未开通FeedCard类型消息!");
                return;
            default:
                logger.warn("[钉钉机器人]消息类型未匹配! msgType={}", msgType);
                return;
        }

        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setAtUserIds(Collections.emptyList());
        req.setAt(at);

        OapiRobotSendResponse rsp = client.execute(req, dingTalkRobotKey);
        logger.debug("钉钉机器人返回信息: {}", rsp.getBody());
    }
}
