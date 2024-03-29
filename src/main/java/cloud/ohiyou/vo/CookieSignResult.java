package cloud.ohiyou.vo;

public class CookieSignResult {
    // 基于索引的cookie名称
    private String cookieName;
    private SignResultVO signResult;
    private long duration;

    public CookieSignResult(String cookieName, SignResultVO signResult, long duration) {
        this.cookieName = cookieName;
        this.signResult = signResult;
        this.duration = duration;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
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
