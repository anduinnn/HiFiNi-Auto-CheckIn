package cloud.ohiyou.service.impl;

import cloud.ohiyou.service.ISignService;
import cloud.ohiyou.utils.OkHttpUtils;
import cloud.ohiyou.vo.SignResultVO;
import cloud.ohiyou.vo.UserInfoVO;
import com.alibaba.fastjson2.JSON;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ohiyou
 * 2025/7/12 15:12
 */

public class HifitiSignService implements ISignService {

    private static final Logger logger = LoggerFactory.getLogger(HifitiSignService.class);

    private static final OkHttpClient client = OkHttpUtils.getClient();
    private static final String SIGN_URL = "https://www.hifiti.com/sg_sign.htm";
    private static final String USER_INFO_URL = "https://www.hifiti.com/my.htm";

    // User-Agent 池
    private static final List<String> USER_AGENTS = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Safari/605.1.15"
    );

    private final Random random = new Random();

    /**
     * 签到
     *
     * @param cookie cookie
     * @return 签到结果
     */
    @Override
    public SignResultVO signIn(String cookie) {
        try {
            String userAgent = getRandomUserAgent();

            RequestBody emptyBody = RequestBody.create("", MediaType.get("application/x-www-form-urlencoded; charset=UTF-8"));

            Request request = new Request.Builder()
                    .url(SIGN_URL)
                    .post(emptyBody)
                    .addHeader("Cookie", cookie)
                    .addHeader("User-Agent", userAgent)
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("Referer", "https://www.hifiti.com/sg_sign.htm")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return new SignResultVO(0, "请求失败，Http状态码：" + response.code());
                }

                String responseBody = readResponse(response);
                logger.info("签到响应内容: {}", responseBody);
                return JSON.parseObject(responseBody, SignResultVO.class);
            }
        } catch (Exception e) {
            logger.error("签到异常: {}", e.getMessage(), e);
            return new SignResultVO(0, "签到异常：" + e.getMessage());
        }
    }

    /**
     * 读取响应内容
     */
    private String readResponse(Response response) throws IOException {
        if (response.body() == null) {
            throw new IOException("Response body is null");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
            StringBuilder result = new StringBuilder();
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                result.append(readLine);
            }
            return result.toString();
        }
    }

    /**
     * 获取随机的 User-Agent
     */
    private String getRandomUserAgent() {
        return USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
    }

    /**
     * 获取用户信息
     *
     * @param cookie cookie
     * @return 用户信息
     */
    @Override
    public UserInfoVO getUserInfo(String cookie) {
        try {
            String userAgent = getRandomUserAgent();

            Request request = new Request.Builder()
                    .url(USER_INFO_URL)
                    .get()
                    .addHeader("Cookie", cookie)
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Referer", "https://www.hifiti.com/")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("获取用户信息失败，HTTP状态码: " + response.code());
                }

                String pageContent = readResponse(response);
                return parseUserInfo(pageContent);
            }

        } catch (Exception e) {
            logger.error("获取用户信息异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取连续签到天数
     *
     * @param cookie 用户cookie
     * @return 连续签到天数
     */
    @Override
    public Integer getSignStreak(String cookie) {
        try {
            String userAgent = getRandomUserAgent();

            Request request = new Request.Builder()
                    .url(SIGN_URL)
                    .get()
                    .addHeader("Cookie", cookie)
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Referer", "https://www.hifiti.com/")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return null;
                }

                String pageContent = readResponse(response);
                return parseSignStreak(pageContent);
            }

        } catch (Exception e) {
            logger.error("获取连续签到天数异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从my.htm页面内容解析用户信息
     */
    private UserInfoVO parseUserInfo(String pageContent) {
        UserInfoVO userInfo = new UserInfoVO();

        try {
            // 解析用户名: <a class="nav-link" href="my.htm"><img class="avatar-1" src="view/img/avatar.png">xx</a>
            Pattern namePattern = Pattern.compile("<li class=\"nav-item username\"><a class=\"nav-link\" href=\"my.htm\"><img class=\"avatar-1\" src=\".*?\"> (.*?)</a></li>");
            Matcher nameMatcher = namePattern.matcher(pageContent);
            if (nameMatcher.find()) {
                userInfo.setUserName(nameMatcher.group(1).trim());
            }

            // 解析金币数量: <span class="text-muted">金币：</span><em style="color: #f57e42;font-style: normal;font-weight: bolder;">6</em>
            Pattern coinPattern = Pattern.compile("<span class=\"text-muted\">金币：</span><em style=\"color: #f57e42;font-style: normal;font-weight: bolder;\">(\\d+)</em>");
            Matcher coinMatcher = coinPattern.matcher(pageContent);
            if (coinMatcher.find()) {
                userInfo.setCoins(Integer.parseInt(coinMatcher.group(1)));
            }

        } catch (Exception e) {
            logger.error("解析用户信息时出错: {}", e.getMessage(), e);
        }

        return userInfo;
    }

    /**
     * 从sg_sign.htm页面解析连续签到天数
     */
    private Integer parseSignStreak(String signPageContent) {
        try {
            // 匹配 JavaScript 变量中的连续签到天数
            Pattern streakPattern = Pattern.compile("var s3 = '连续签到(\\d+)天';");
            Matcher streakMatcher = streakPattern.matcher(signPageContent);
            if (streakMatcher.find()) {
                int streak = Integer.parseInt(streakMatcher.group(1));
                logger.debug("解析连续签到天数成功: {} 天", streak);
                return streak;
            } else {
                logger.debug("页面中未找到连续签到天数信息");
                // 如果找不到，尝试更宽松的匹配
                Pattern fallbackPattern = Pattern.compile("连续签到(\\d+)天");
                Matcher fallbackMatcher = fallbackPattern.matcher(signPageContent);
                if (fallbackMatcher.find()) {
                    int streak = Integer.parseInt(fallbackMatcher.group(1));
                    logger.debug("通过备用方式解析连续签到天数成功: {} 天", streak);
                    return streak;
                }
            }
        } catch (Exception e) {
            logger.error("解析连续签到天数时出错: {}", e.getMessage(), e);
        }
        return null;
    }
}
