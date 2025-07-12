package cloud.ohiyou.vo;

import lombok.Data;

/**
 * @author ohiyou
 * @since 2024/2/21 11:37
 */
@Data
public class CookieSignResult {
    private SignResultVO signResult;
    private long duration;

    public CookieSignResult(SignResultVO signResult, long duration) {
        this.signResult = signResult;
        this.duration = duration;
    }
}
