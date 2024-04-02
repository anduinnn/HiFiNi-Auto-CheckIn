package cloud.ohiyou.vo;

public class CookieSignResult {
    private SignResultVO signResult;
    private long duration;

    public CookieSignResult(SignResultVO signResult, long duration) {
        this.signResult = signResult;
        this.duration = duration;
    }

    public SignResultVO getSignResult() {
        return signResult;
    }

    public void setSignResult(SignResultVO signResult) {
        this.signResult = signResult;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
