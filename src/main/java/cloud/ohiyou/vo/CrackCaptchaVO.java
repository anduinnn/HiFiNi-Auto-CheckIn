package cloud.ohiyou.vo;

/**
 * @author ohiyou
 * @since 2024/3/1 9:32
 */
public class CrackCaptchaVO {
    private String clientKey;
    private CrackCaptchaTaskVO task;

    public CrackCaptchaVO(String clientKey, CrackCaptchaTaskVO task) {
        this.clientKey = clientKey;
        this.task = task;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public CrackCaptchaTaskVO getTask() {
        return task;
    }

    public void setTask(CrackCaptchaTaskVO task) {
        this.task = task;
    }
}
