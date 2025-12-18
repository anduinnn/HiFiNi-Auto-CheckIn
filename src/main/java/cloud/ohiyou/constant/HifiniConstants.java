package cloud.ohiyou.constant;

import java.util.Arrays;
import java.util.List;

/**
 * HiFiNi 项目常量定义
 *
 * @author ohiyou
 */
public final class HifiniConstants {

    private HifiniConstants() {
        // 防止实例化
    }

    // ==================== HiFiTi URL 常量 ====================
    public static final String HIFITI_BASE_URL = "https://www.hifiti.com";
    public static final String HIFITI_SIGN_URL = HIFITI_BASE_URL + "/sg_sign.htm";
    public static final String HIFITI_USER_INFO_URL = HIFITI_BASE_URL + "/my.htm";

    // ==================== HiFiHi URL 常量 ====================
    public static final String HIFIHI_BASE_URL = "https://hifihi.com";
    public static final String HIFIHI_SIGN_URL = HIFIHI_BASE_URL + "/sg_sign.htm";
    public static final String HIFIHI_USER_INFO_URL = HIFIHI_BASE_URL + "/my.htm";

    // 兼容旧代码
    @Deprecated
    public static final String BASE_URL = HIFITI_BASE_URL;
    @Deprecated
    public static final String SIGN_URL = HIFITI_SIGN_URL;
    @Deprecated
    public static final String USER_INFO_URL = HIFITI_USER_INFO_URL;

    // ==================== Cookie 相关 ====================
    public static final String COOKIE_KEY_BBS_SID = "bbs_sid";
    public static final String COOKIE_KEY_BBS_TOKEN = "bbs_token";
    public static final String COOKIE_SEPARATOR = "&";

    // ==================== 线程池配置 ====================
    public static final int DEFAULT_THREAD_POOL_SIZE = 5;
    public static final int DEFAULT_AWAIT_TERMINATION_SECONDS = 20;

    // ==================== HTTP 配置 ====================
    public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 30;
    public static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
    public static final int DEFAULT_WRITE_TIMEOUT_SECONDS = 30;

    // ==================== User-Agent 池 ====================
    public static final List<String> USER_AGENTS = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Safari/605.1.15"
    );

    // ==================== 正则表达式 ====================
    public static final String REGEX_USERNAME = "<li class=\"nav-item username\"><a class=\"nav-link\" href=\"my.htm\"><img class=\"avatar-1\" src=\".*?\"> (.*?)</a></li>";
    public static final String REGEX_COINS = "<span class=\"text-muted\">金币：</span><em style=\"color: #f57e42;font-style: normal;font-weight: bolder;\">(\\d+)</em>";
    public static final String REGEX_SIGN_STREAK = "var s3 = '连续签到(\\d+)天';";
    public static final String REGEX_SIGN_STREAK_FALLBACK = "连续签到(\\d+)天";
}
