package cloud.ohiyou.service;

/**
 * 推送消息的抽象类
 */
public interface IMessagePushStrategy {


    /**
     * @param title   推送的标题
     * @param message 推送的消息（默认是markdown格式）
     */
    void pushMessage(String title, String message);
}
