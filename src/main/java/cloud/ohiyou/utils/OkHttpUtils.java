package cloud.ohiyou.utils;

import cloud.ohiyou.constant.HifiniConstants;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * OkHttp 客户端工具类
 * 提供全局唯一的 OkHttpClient 实例
 *
 * @author ohiyou
 */
public final class OkHttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtils.class);

    private static volatile OkHttpClient client;

    private static final Object LOCK = new Object();

    /**
     * 禁止实例化
     */
    private OkHttpUtils() {
    }

    /**
     * 获取 OkHttpClient 单例实例
     * 使用双重检查锁定确保线程安全
     *
     * @return OkHttpClient 实例
     */
    public static OkHttpClient getClient() {
        if (client == null) {
            synchronized (LOCK) {
                if (client == null) {
                    client = createClient();
                }
            }
        }
        return client;
    }

    /**
     * 创建 OkHttpClient 实例
     */
    private static OkHttpClient createClient() {
        // 自定义守护线程池，解决 exec 命令中线程无法释放问题
        ExecutorService executor = new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                r -> {
                    Thread thread = new Thread(r, "OkHttp-Daemon");
                    thread.setDaemon(true);
                    return thread;
                }
        );

        return new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .dispatcher(new Dispatcher(executor))
                .connectTimeout(HifiniConstants.DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HifiniConstants.DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(HifiniConstants.DEFAULT_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 关闭 OkHttpClient 资源
     * 应在应用退出时调用
     */
    public static void shutdown() {
        if (client != null) {
            try {
                client.dispatcher().executorService().shutdownNow();
                client.connectionPool().evictAll();
                if (client.cache() != null) {
                    client.cache().close();
                }
                logger.info("OkHttpClient 资源已释放");
            } catch (Exception e) {
                logger.error("关闭 OkHttpClient 时发生异常: {}", e.getMessage(), e);
            }
        }
    }
}
