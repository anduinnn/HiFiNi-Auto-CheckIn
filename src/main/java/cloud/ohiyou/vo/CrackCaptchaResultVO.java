package cloud.ohiyou.vo;

/**
 * @author ohiyou
 * @since 2024/3/1 9:32
 */
public class CrackCaptchaResultVO {
    private String errorId;
    private String status;
    private String taskId;


    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
