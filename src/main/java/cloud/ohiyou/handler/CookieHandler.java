package cloud.ohiyou.handler;

import cloud.ohiyou.constant.HifiniConstants;
import cloud.ohiyou.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Cookie 处理器
 * 负责 Cookie 的解析、验证和格式化
 *
 * @author ohiyou
 */
public class CookieHandler {

    private static final Logger logger = LoggerFactory.getLogger(CookieHandler.class);

    /**
     * 格式化 Cookie，提取 bbs_sid 和 bbs_token
     *
     * @param cookie 原始 Cookie 字符串
     * @param index  Cookie 索引（用于日志）
     * @return 格式化后的 Cookie，如果格式无效则返回 empty
     */
    public Optional<String> formatCookie(String cookie, int index) {
        logger.debug("开始解析第 {} 个cookie", index + 1);

        if (StringUtils.isBlank(cookie)) {
            logger.warn("第 {} 个cookie为空", index + 1);
            return Optional.empty();
        }

        String bbsSid = null;
        String bbsToken = null;

        String[] parts = cookie.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith(HifiniConstants.COOKIE_KEY_BBS_SID + "=")) {
                bbsSid = trimmed;
                logger.debug("找到bbs_sid");
            } else if (trimmed.startsWith(HifiniConstants.COOKIE_KEY_BBS_TOKEN + "=")) {
                bbsToken = trimmed;
                logger.debug("找到bbs_token");
            }
        }

        if (bbsSid != null && bbsToken != null) {
            logger.info("成功解析第 {} 个cookie", index + 1);
            return Optional.of(bbsSid + ";" + bbsToken + ";");
        }

        logger.error("解析第 {} 个cookie失败，格式不正确。缺少: {}",
                index + 1,
                bbsSid == null ? HifiniConstants.COOKIE_KEY_BBS_SID : HifiniConstants.COOKIE_KEY_BBS_TOKEN);
        return Optional.empty();
    }

    /**
     * 分割多个 Cookie 字符串
     *
     * @param cookies 以 & 分隔的多个 Cookie
     * @return Cookie 数组
     */
    public String[] splitCookies(String cookies) {
        if (StringUtils.isBlank(cookies)) {
            return new String[0];
        }
        return cookies.split(HifiniConstants.COOKIE_SEPARATOR);
    }
}
