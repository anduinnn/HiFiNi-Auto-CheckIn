package cloud.ohiyou.utils;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 钉钉工具箱
 * @Author 银垚@mtouyao
 * @Date&Time 2024/3/7 10:25
 */
public class DingTalkUtils {

    /**
     * 钉钉机器人
     * @Author 银垚@mtouyao
     * @Date&Time 2024/3/7 10:25
     * @param customRobotToken 你的机器人应用的 access_token 的值/your custom robot token
     * @param messageText 签到信息
     * @param userIDs (可选)用户的 userId 信息[仅限钉钉内部群使用]/you need @ group user's userId
     * @param msgType (必选其一)text/markdown
     */
    public static void pushBotMessage(String customRobotToken, String messageTitle, String messageText, String userIDs, String msgType){
        try {
            if (customRobotToken == null || "".equals(customRobotToken)) {
                System.out.println("DINGTALK_WEBHOOK 环境变量未设置");
                return;
            }
            if (msgType == null) {msgType="markdown";}
            Long timestamp = System.currentTimeMillis();
//            System.out.println(timestamp); // 时间戳
            String secret = "messageKey"; // 安全设置，加签密钥。
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
//            System.out.println(sign); // 签名

            //sign字段和timestamp字段必须拼接到请求URL上，否则会出现 310000 的错误信息
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?sign="+sign+"&timestamp="+timestamp);
            OapiRobotSendRequest req = new OapiRobotSendRequest();

            switch (msgType) {
                // 钉钉发送类型详解官网: https://open.dingtalk.com/document/orgapp/custom-robot-access
                // 发送text消息
                case "text":
                    //定义文本内容
                    OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
                    text.setContent("HiFiNi签到信息:"+messageTitle+messageText);
                    //设置消息类型
                    req.setMsgtype("text");
                    req.setText(text);
                    break;
                //发送markdown消息
                case "markdown":
                    // 定义markdown内容
                    OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
                    markdown.setTitle("HiFiNiBot消息通知");
                    markdown.setText("# HiFiNi签到消息通知 \n ## "+messageTitle+" \n"+messageText);
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
            at.setAtUserIds(Arrays.asList(userIDs));
            req.setAt(at);
            OapiRobotSendResponse rsp = client.execute(req, customRobotToken);
            System.out.println("钉钉机器人返回信息："+rsp.getBody());
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
