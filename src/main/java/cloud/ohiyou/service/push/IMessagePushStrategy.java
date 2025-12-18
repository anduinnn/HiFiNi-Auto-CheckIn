package cloud.ohiyou.service.push;

/**
 * 消息推送策略接口
 *
 * @author ohiyou
 */
public interface IMessagePushStrategy {

    /**
     * 推送消息
     *
     * @param title   推送的标题
     * @param message 推送的消息（默认是markdown格式）
     */
    void pushMessage(String title, String message);
}
