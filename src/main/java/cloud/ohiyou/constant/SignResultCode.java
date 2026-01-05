package cloud.ohiyou.constant;

/**
 * 签到结果码枚举
 *
 * @author ohiyou
 */
public enum SignResultCode {

    SUCCESS(0, "签到成功"),
    ALREADY_SIGNED(-1, "今日已签到"),
    FAILURE(2, "签到失败"),
    COOKIE_INVALID(401, "Cookie已失效"),
    NETWORK_ERROR(500, "网络错误"),
    UNKNOWN_ERROR(999, "未知错误");

    private final int code;
    private final String description;

    SignResultCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code值获取对应的枚举
     *
     * @param code 结果码
     * @return 对应的枚举值，如果不存在则返回 UNKNOWN_ERROR
     */
    public static SignResultCode fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN_ERROR;
        }
        for (SignResultCode rc : values()) {
            if (rc.code == code) {
                return rc;
            }
        }
        return UNKNOWN_ERROR;
    }

    /**
     * 判断是否为成功状态（签到成功或今日已签到）
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return this == SUCCESS || this == ALREADY_SIGNED;
    }
}
