package cloud.ohiyou;

/**
 * 腾讯云函数 (SCF) 入口类
 *
 * @author ohiyou
 */
public class ScfHandler {

    /**
     * 腾讯云函数入口方法
     * 函数配置：
     * - 执行方法：cloud.ohiyou.ScfHandler::mainHandler
     * - 运行环境：Java 8
     *
     * @param event 触发事件（定时触发器传入的 JSON）
     * @return 执行结果
     */
    public String mainHandler(String event) {
        try {
            HifiniApplication.main(new String[]{});
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}
