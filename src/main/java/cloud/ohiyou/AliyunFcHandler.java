package cloud.ohiyou;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 阿里云函数计算 (FC) 入口类
 *
 * @author ohiyou
 */
public class AliyunFcHandler implements StreamRequestHandler {

    /**
     * 阿里云函数计算入口方法
     * 函数配置：
     * - 请求处理程序：cloud.ohiyou.AliyunFcHandler::handleRequest
     * - 运行环境：Java 8
     *
     * @param input   输入流
     * @param output  输出流
     * @param context 函数上下文
     * @throws IOException IO异常
     */
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        try {
            HifiniApplication.main(new String[]{});
            output.write("success".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            output.write(("error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }
}
