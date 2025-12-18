package cloud.ohiyou.exception;

/**
 * Cookie 无效异常
 *
 * @author ohiyou
 */
public class CookieInvalidException extends HifiniException {

    public CookieInvalidException(String message) {
        super(message);
    }

    public CookieInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
