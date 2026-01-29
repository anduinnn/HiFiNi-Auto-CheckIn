package cloud.ohiyou.service.push;

import cloud.ohiyou.constant.PushPlatform;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 推送策略抽象基类
 * 提供统一的异常处理、日志记录和辅助方法
 *
 * @author ohiyou
 */
public abstract class AbstractPushStrategy implements IMessagePushStrategy {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final OkHttpClient client;
    protected final PushPlatform platform;

    protected AbstractPushStrategy(OkHttpClient client, PushPlatform platform) {
        this.client = client;
        this.platform = platform;
    }

    @Override
    public final void pushMessage(String title, String message) {
        logger.debug("开始推送消息到 {}", platform.getDisplayName());
        try {
            doPush(title, message);
            logger.info("推送到 {} 成功", platform.getDisplayName());
        } catch (Exception e) {
            logger.error("推送到 {} 失败: {}", platform.getDisplayName(), e.getMessage(), e);
        }
    }

    /**
     * 执行实际的推送操作
     * 子类需要实现此方法
     *
     * @param title   标题
     * @param message 消息内容
     * @throws Exception 推送过程中的异常
     */
    protected abstract void doPush(String title, String message) throws Exception;

    /**
     * 获取推送平台
     *
     * @return 推送平台枚举
     */
    public PushPlatform getPlatform() {
        return platform;
    }

    /**
     * 执行 HTTP 请求
     *
     * @param request 请求对象
     * @return 响应对象
     * @throws IOException 网络异常
     */
    protected Response executeRequest(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    /**
     * URL 编码
     *
     * @param value 待编码的值
     * @return 编码后的值
     */
    protected String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 总是支持的，这个异常不应该发生
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }
}
