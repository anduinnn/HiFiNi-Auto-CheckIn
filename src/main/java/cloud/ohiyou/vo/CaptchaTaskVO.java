package cloud.ohiyou.vo;

/**
 * @author ohiyou
 * @since 2024/3/1 9:52
 */
public class CaptchaTaskVO {
    private String clientKey;
    private String taskId;

    public CaptchaTaskVO(String clientKey, String taskId) {
        this.clientKey = clientKey;
        this.taskId = taskId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
