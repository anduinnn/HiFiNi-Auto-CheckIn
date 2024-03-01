package cloud.ohiyou.vo;

/**
 * @author ohiyou
 * @since 2024/2/21 11:37
 */
public class SignResultVO {
    private Integer code;
    private String message;

    public SignResultVO(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
