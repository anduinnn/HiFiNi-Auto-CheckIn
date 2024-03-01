package cloud.ohiyou.vo;

/**
 * @author ohiyou
 * @since 2024/3/1 9:52
 */
public class CaptchaTaskResultVO {
    private String errorId;
    private String taskId;
    private String ready;
    private String status;
    private SolutionVO solution;

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getReady() {
        return ready;
    }

    public void setReady(String ready) {
        this.ready = ready;
    }

    public SolutionVO getSolution() {
        return solution;
    }

    public void setSolution(SolutionVO solution) {
        this.solution = solution;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
