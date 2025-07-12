package cloud.ohiyou.utils;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.util.concurrent.*;

/**
 * @author ohiyou
 * 2025/4/26 16:33
 */
public class OkHttpUtils {

    // 自定义守护线程,为了解决exec命令中线程无法被释放问题
    private static final ExecutorService OK_EXEC = new ThreadPoolExecutor(0,Integer.MAX_VALUE,
            60L,TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> {
                Thread thread = new Thread(r, "OkHttp");
                thread.setDaemon(true);
                return thread;
            });

    public static final OkHttpClient client = new OkHttpClient.Builder()
            .dispatcher(new Dispatcher(OK_EXEC))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 禁止实例化，一般工具类只需要通过 类名.方法 来访问
     */
    private OkHttpUtils() {
    }

    public static OkHttpClient getClient() {
        return client;
    }
}
