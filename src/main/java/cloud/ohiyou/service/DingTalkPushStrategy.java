package cloud.ohiyou.service;

import cloud.ohiyou.config.EnvConfig;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author ohiyou
 * 2025/4/26 16:33
 */
public class DingTalkPushStrategy implements IMessagePushStrategy {


    /**
     * 钉钉机器人
     *
     * @param title   title
     * @param message message
     * @Author 银垚@mtouyao
     * @Date&Time 2024/3/7
     */
    @Override
    public void pushMessage(String title, String message) {
        try {
            String dingTalkRobotKey = EnvConfig.get().getDingTalkRobotKey();
            String msgType = "markdown";
            Long timestamp = System.currentTimeMillis();
//            System.out.println(timestamp); // 时间戳
            String secret = "messageKey"; // 安全设置，加签密钥。
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
//            System.out.println(sign); // 签名

            //sign字段和timestamp字段必须拼接到请求URL上，否则会出现 310000 的错误信息
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?sign=" + sign + "&timestamp=" + timestamp);
            OapiRobotSendRequest req = new OapiRobotSendRequest();

            switch (msgType) {
                // 钉钉发送类型详解官网: https://open.dingtalk.com/document/orgapp/custom-robot-access
                // 发送text消息
                case "text":
                    //定义文本内容
                    OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
                    text.setContent("HiFiNiBot签到信息:" + title + message);
                    //设置消息类型
                    req.setMsgtype("text");
                    req.setText(text);
                    break;
                //发送markdown消息
                case "markdown":
                    // 定义markdown内容
                    OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
                    markdown.setTitle("HiFiNiBot消息通知");
                    markdown.setText("# HiFiNiBot签到消息通知 \n ## " + title + " \n" + message);
                    req.setMsgtype("markdown");
                    req.setMarkdown(markdown);
                    break;

                // 发送link消息
                case "link":
                    System.out.println("[钉钉机器人]暂未开通link类型消息!");
                    break;
                // 发送ActionCard消息
                case "ActionCard":
                    System.out.println("[钉钉机器人]暂未开通ActionCard类型消息!");
                    break;
                // 发送FeedCard消息
                case "FeedCard":
                    System.out.println("[钉钉机器人]暂未开通FeedCard类型消息!");
                    break;

                /** 未匹配 */
                default:
                    System.out.println("[钉钉机器人]消息类型未匹配!请检查调用pushBotMessage传递的msgType参数");
                    break;

            }

            //定义 @ 对象
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtUserIds(Arrays.asList());
            req.setAt(at);
            OapiRobotSendResponse rsp = client.execute(req, dingTalkRobotKey);
            System.out.println("钉钉机器人返回信息：" + rsp.getBody());
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
