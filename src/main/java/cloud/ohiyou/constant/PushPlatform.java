package cloud.ohiyou.constant;

/**
 * 推送平台枚举
 *
 * @author ohiyou
 */
public enum PushPlatform {

    SERVER_CHAN("Server酱"),
    WECHAT_WORK("企业微信机器人"),
    DINGTALK("钉钉机器人"),
    TELEGRAM("Telegram机器人"),
    GOTIFY("Gotify");

    private final String displayName;

    PushPlatform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
