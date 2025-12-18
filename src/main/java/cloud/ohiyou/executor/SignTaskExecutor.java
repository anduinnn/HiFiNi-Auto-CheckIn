package cloud.ohiyou.executor;

import cloud.ohiyou.constant.HifiniConstants;
import cloud.ohiyou.handler.CookieHandler;
import cloud.ohiyou.service.ISignService;
import cloud.ohiyou.vo.CookieSignResult;
import cloud.ohiyou.vo.SignResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * 签到任务执行器
 * 负责管理线程池和执行签到任务
 *
 * @author ohiyou
 */
public class SignTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SignTaskExecutor.class);

    private final ExecutorService executor;
    private final int awaitTerminationSeconds;

    /**
     * 使用默认配置创建执行器
     */
    public SignTaskExecutor() {
        this(HifiniConstants.DEFAULT_THREAD_POOL_SIZE,
                HifiniConstants.DEFAULT_AWAIT_TERMINATION_SECONDS);
    }

    /**
     * 使用自定义配置创建执行器
     *
     * @param threadPoolSize           线程池大小
     * @param awaitTerminationSeconds  等待终止超时时间（秒）
     */
    public SignTaskExecutor(int threadPoolSize, int awaitTerminationSeconds) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }

    /**
     * 执行签到任务
     *
     * @param cookies       Cookie 数组
     * @param signService   签到服务
     * @param cookieHandler Cookie 处理器
     * @return 签到结果列表
     */
    public List<CookieSignResult> executeSignTasks(String[] cookies,
                                                    ISignService signService,
                                                    CookieHandler cookieHandler) {
        List<CookieSignResult> results = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < cookies.length; i++) {
            final int index = i;
            final String cookie = cookies[i];

            Future<?> future = executor.submit(() ->
                    processSignTask(cookie, index, signService, cookieHandler, results));
            futures.add(future);
        }

        waitForCompletion(futures);

        return results;
    }

    /**
     * 处理单个签到任务
     */
    private void processSignTask(String cookie, int index, ISignService signService,
                                  CookieHandler cookieHandler, List<CookieSignResult> results) {
        long startTime = System.currentTimeMillis();
        logger.info("开始处理第 {} 个cookie", index + 1);

        try {
            Optional<String> formattedCookie = cookieHandler.formatCookie(cookie.trim(), index);
            if (formattedCookie.isPresent()) {
                SignResultVO signResult = signService.signInWithUserInfo(formattedCookie.get());
                long duration = System.currentTimeMillis() - startTime;
                results.add(new CookieSignResult(signResult, duration));
                logger.info("第 {} 个用户签到完成，耗时: {}ms", index + 1, duration);
            } else {
                // Cookie 格式无效
                results.add(new CookieSignResult(
                        new SignResultVO(0, "Cookie格式无效"), 0));
            }
        } catch (Exception e) {
            logger.error("处理第 {} 个cookie时发生错误: {}", index + 1, e.getMessage(), e);
            results.add(new CookieSignResult(
                    new SignResultVO(0, "签到失败: " + e.getMessage()), 0));
        }
    }

    /**
     * 等待所有任务完成
     */
    private void waitForCompletion(List<Future<?>> futures) {
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
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(awaitTerminationSeconds, TimeUnit.SECONDS)) {
                logger.warn("线程池未在指定时间内完成，强制关闭");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("等待线程池关闭时被中断，强制关闭");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
