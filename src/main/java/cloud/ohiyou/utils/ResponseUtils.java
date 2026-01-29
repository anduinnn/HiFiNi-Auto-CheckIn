package cloud.ohiyou.utils;

import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 响应处理工具类
 *
 * @author ohiyou
 */
public final class ResponseUtils {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtils.class);

    private ResponseUtils() {
        // 防止实例化
    }

    /**
     * 读取 HTTP 响应内容
     *
     * @param response OkHttp 响应对象
     * @return 响应内容字符串
     * @throws IOException 如果响应体为空或读取失败
     */
    public static String readResponse(Response response) throws IOException {
        if (response == null || response.body() == null) {
            throw new IOException("Response or response body is null");
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }
}
