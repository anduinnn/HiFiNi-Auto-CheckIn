package cloud.ohiyou.actions.vo;

/**
 * @author ohiyou
 * @since 2024/2/21 11:37
 */
public class SignResultVO {
    private Integer code;
    private String message;
    private String userName;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    @Override
    public String toString() {
        return "SignResultVO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
