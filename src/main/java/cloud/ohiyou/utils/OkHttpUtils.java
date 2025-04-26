package cloud.ohiyou.utils;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * @author ohiyou
 * 2025/4/26 16:33
 */
public class OkHttpUtils {
    public static final OkHttpClient client = new OkHttpClient.Builder()
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
