package cloud.ohiyou.exception;

/**
 * HiFiNi 自定义异常基类
 *
 * @author ohiyou
 */
public class HifiniException extends RuntimeException {

    public HifiniException(String message) {
        super(message);
    }

    public HifiniException(String message, Throwable cause) {
        super(message, cause);
    }
}
