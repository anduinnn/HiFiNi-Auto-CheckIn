package cloud.ohiyou;

import cloud.ohiyou.config.EnvConfig;
import cloud.ohiyou.factory.PushStrategyFactory;
import cloud.ohiyou.service.IMessagePushStrategy;
import cloud.ohiyou.service.ISignService;
import cloud.ohiyou.service.impl.HifitiSignService;
import cloud.ohiyou.utils.OkHttpUtils;
import cloud.ohiyou.vo.CookieSignResult;
import cloud.ohiyou.vo.SignResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author ohiyou
 * 2025/7/12 16:43
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ISignService signService = new HifitiSignService();


    public static void main(String[] args) {
        logger.info("HiFiTi自动签到任务开始");

        List<CookieSignResult> results = Collections.synchronizedList(new ArrayList<>());
        String cookies = EnvConfig.get().getCookie();
        if (cookies == null) {
            logger.error("未设置Cookie环境变量");
            throw new RuntimeException("未设置Cookie");
        }

        String[] cookiesArray = cookies.split("&");
        logger.info(" 检测到 {} 个cookie，开始签到", cookiesArray.length);

        // 创建一个固定线程数的线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();


        for (int i = 0; i < cookiesArray.length; i++) {
            final String cookie = cookiesArray[i];
            final int index = i;
            Future<?> future = executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    logger.info("开始处理第 {} 个cookie: {}", index + 1, cookie);

                    // 格式化cookie，去除空格等，同时检测cookie格式
                    String formattedCookie = formatCookie(cookie.trim(), index);
                    if (formattedCookie != null) {
                        // 执行签到
                        SignResultVO signResultVO = signService.signInWithUserInfo(formattedCookie);
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;

                        results.add(new CookieSignResult(signResultVO, duration));
                        logger.info(" 第 {} 个用户签到完成，耗时: {}ms", index + 1, duration);
                    }
                } catch (Exception e) {
                    logger.error(" 处理第 {} 个cookie时发生错误: {}", index + 1, e.getMessage(), e);
                    // 添加失败的结果
                    results.add(new CookieSignResult(new SignResultVO(0, "签到失败: " + e.getMessage()), 0));
                }
            });
            futures.add(future);
        }

        // 关闭线程池，使其不再接受新任务
        executor.shutdown();

        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("当前线程在等待任务完成时被中断");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                logger.error("执行任务时出错: {}", cause.getMessage(), cause);
            }
        }

        // 等待线程池完全终止
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                logger.warn("线程池未在指定时间内完成，强制关闭");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("等待线程池关闭时被中断，强制关闭");
            executor.shutdownNow();
        }

        publishResults(results);

        // 关闭OkHttpClient
        OkHttpUtils.getClient().dispatcher().executorService().shutdownNow();
        OkHttpUtils.getClient().connectionPool().evictAll();
        logger.info("HiFiTi自动签到任务完成");
    }

    /**
     * 推送签到结果
     *
     * @param results results，每个cookie的签到结果
     */
    private static void publishResults(List<CookieSignResult> results) {
        logger.info("开始推送签到结果");

        StringBuilder messageBuilder = new StringBuilder();
        boolean allSuccess = true;

        if (results == null || results.isEmpty()) {
            allSuccess = false;
            messageBuilder.append("未获取到任何签到结果，请检查任务执行情况。\n");
            logger.warn("未获取到任何签到结果");
        } else {
            for (CookieSignResult result : results) {
                SignResultVO signResult = result.getSignResult();
                String userName = signResult.getUserName() != null ? signResult.getUserName() : "未知用户";

                messageBuilder.append(userName).append(":\n");

                // 用户信息
                messageBuilder.append(signResult.getFormattedMessage());
                messageBuilder.append("\n\n耗时: ").append(result.getDuration()).append("ms\n");

                // 添加分隔线
                messageBuilder.append("\n\n────────────────────\n\n");

                // 检查是否成功
                if (signResult.getCode() == null || signResult.getCode() != 1) {
                    allSuccess = false;
                }
            }
        }

        String title = allSuccess ? " HiFiTi签到成功" : "HiFiTi签到失败";

        // 添加总结信息
        int successCount = 0;
        int totalCount = results.size();
        for (CookieSignResult result : results) {
            if (result.getSignResult().getCode() != null && result.getSignResult().getCode() == 1) {
                successCount++;
            }
        }

        String summary = String.format("\n签到统计: %d/%d 成功", successCount, totalCount);
        messageBuilder.append(summary);

        logger.info("签到结果: {}", title);
        logger.info("成功率: {}/{}", successCount, totalCount);
        logger.debug("完整消息内容:\n{}", messageBuilder);

        // 推送
        List<IMessagePushStrategy> strategies = PushStrategyFactory.getStrategy();
        for (IMessagePushStrategy strategy : strategies) {
            try {
                strategy.pushMessage(title, messageBuilder.toString());
                logger.debug("推送策略 {} 执行成功", strategy.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("推送策略 {} 执行失败: {}", strategy.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        logger.info("签到结果推送完成");
    }

    /**
     * 检测cookie的格式，是否包含bbs_sid和bbs_token
     * 去除多余的空格和空字符串
     *
     * @param cookie cookie
     * @param index  index
     * @return formatCookie
     */
    private static String formatCookie(String cookie, int index) {
        logger.debug("开始解析第 {} 个cookie", index + 1);

        String bbsSid = null;
        String bbsToken = null;

        // 分割cookie字符串
        String[] split = cookie.split(";");

        // 遍历分割后的字符串数组
        for (String s : split) {
            s = s.trim(); // 去除可能的前后空格
            // 检查当前字符串是否包含bbs_sid或bbs_token
            if (s.startsWith("bbs_sid=")) {
                bbsSid = s;
                logger.debug("找到bbs_sid");
            } else if (s.startsWith("bbs_token=")) {
                bbsToken = s;
                logger.debug(" 找到bbs_token");
            }
        }

        // 确保bbs_sid和bbs_token都不为空
        if (bbsSid != null && bbsToken != null) {
            logger.info("成功解析第 {} 个cookie", index + 1);
            return bbsSid + ";" + bbsToken + ";";
        } else {
            logger.error(" 解析第 {} 个cookie失败，格式不正确。缺少: {}",
                    index + 1,
                    bbsSid == null ? "bbs_sid" : "bbs_token");
            return null;
        }
    }
}
