package cloud.ohiyou;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.executor.SignTaskExecutor;
import cloud.ohiyou.factory.PushStrategyFactory;
import cloud.ohiyou.handler.CookieHandler;
import cloud.ohiyou.handler.ResultPublisher;
import cloud.ohiyou.service.ISignService;
import cloud.ohiyou.service.impl.HifihiSignService;
import cloud.ohiyou.service.impl.HifitiSignService;
import cloud.ohiyou.service.push.IMessagePushStrategy;
import cloud.ohiyou.utils.OkHttpUtils;
import cloud.ohiyou.vo.CookieSignResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * HiFiNi 自动签到应用入口
 * 支持 HiFiTi 和 HiFiHi 两个站点的签到
 *
 * @author ohiyou
 */
public class HifiniApplication {

    private static final Logger logger = LoggerFactory.getLogger(HifiniApplication.class);

    public static void main(String[] args) {
        logger.info("自动签到任务开始");

        List<CookieSignResult> allResults = new ArrayList<>();
        CookieHandler cookieHandler = new CookieHandler();
        SignTaskExecutor executor = new SignTaskExecutor();

        try {
            EnvConfig config = EnvConfig.get();

            // 执行 HiFiTi 签到
            String hifitiCookies = config.getCookie();
            if (hifitiCookies != null && !hifitiCookies.trim().isEmpty()) {
                logger.info("===== HiFiTi 站点签到开始 =====");
                List<CookieSignResult> hifitiResults = executeSignIn(
                        hifitiCookies, new HifitiSignService(), cookieHandler, executor, "HiFiTi");
                allResults.addAll(hifitiResults);
            } else {
                logger.info("未设置 HiFiTi Cookie，跳过该站点签到");
            }

            // 执行 HiFiHi 签到
            String hifihiCookies = config.getHifihiCookie();
            if (hifihiCookies != null && !hifihiCookies.trim().isEmpty()) {
                logger.info("===== HiFiHi 站点签到开始 =====");
                List<CookieSignResult> hifihiResults = executeSignIn(
                        hifihiCookies, new HifihiSignService(), cookieHandler, executor, "HiFiHi");
                allResults.addAll(hifihiResults);
            } else {
                logger.info("未设置 HiFiHi Cookie，跳过该站点签到");
            }

            // 检查是否有任何签到任务
            if (allResults.isEmpty()) {
                logger.warn("未配置任何站点的 Cookie，无签到任务执行");
                return;
            }

            // 发布汇总结果
            List<IMessagePushStrategy> strategies = PushStrategyFactory.createStrategies();
            ResultPublisher publisher = new ResultPublisher(strategies);
            publisher.publish(allResults);

        } catch (Exception e) {
            logger.error("签到任务执行失败: {}", e.getMessage(), e);
        } finally {
            executor.shutdown();
            OkHttpUtils.shutdown();
            logger.info("自动签到任务完成");
        }
    }

    /**
     * 执行指定站点的签到任务
     *
     * @param cookies       cookie字符串
     * @param signService   签到服务
     * @param cookieHandler cookie处理器
     * @param executor      任务执行器
     * @param siteName      站点名称
     * @return 签到结果列表
     */
    private static List<CookieSignResult> executeSignIn(
            String cookies,
            ISignService signService,
            CookieHandler cookieHandler,
            SignTaskExecutor executor,
            String siteName) {

        String[] cookieArray = cookieHandler.splitCookies(cookies);
        logger.info("{}: 检测到 {} 个cookie，开始签到", siteName, cookieArray.length);

        List<CookieSignResult> results = executor.executeSignTasks(cookieArray, signService, cookieHandler);

        // 为结果添加站点标识
        for (CookieSignResult result : results) {
            if (result.getSignResult() != null) {
                String originalUserName = result.getSignResult().getUserName();
                result.getSignResult().setUserName("[" + siteName + "] " +
                        (originalUserName != null ? originalUserName : "未知用户"));
            }
        }

        logger.info("{}: 签到任务完成，共 {} 个结果", siteName, results.size());
        return results;
    }
}
