package cloud.ohiyou.utils;

/**
 * @author ohiyou
 * 2025/4/26 16:33
 */
public class StringUtils {

    private StringUtils() {
    }

    /**
     * 判断字符串是否为空
     * 当字符串是 null 或者 "" 时返回true
     *
     * @param str str
     * @return true: null or empty; false: not null and not empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否为空
     * 当字符串是 null 或者 "" 或者 " " 时返回true
     *
     * @param str str
     * @return true or false
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
