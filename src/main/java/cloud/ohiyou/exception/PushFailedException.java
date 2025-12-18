package cloud.ohiyou.exception;

import cloud.ohiyou.constant.PushPlatform;

/**
 * 推送失败异常
 *
 * @author ohiyou
 */
public class PushFailedException extends HifiniException {

    private final PushPlatform platform;

    public PushFailedException(PushPlatform platform, String message) {
        super(String.format("推送到 %s 失败: %s", platform.getDisplayName(), message));
        this.platform = platform;
    }

    public PushFailedException(PushPlatform platform, String message, Throwable cause) {
        super(String.format("推送到 %s 失败: %s", platform.getDisplayName(), message), cause);
        this.platform = platform;
    }

    public PushPlatform getPlatform() {
        return platform;
    }
}
