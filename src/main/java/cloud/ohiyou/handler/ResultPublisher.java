package cloud.ohiyou.handler;

import cloud.ohiyou.constant.SignResultCode;
import cloud.ohiyou.service.push.IMessagePushStrategy;
import cloud.ohiyou.vo.CookieSignResult;
import cloud.ohiyou.vo.SignResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 结果发布器
 * 负责构建签到结果消息并推送到各个平台
 *
 * @author ohiyou
 */
public class ResultPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ResultPublisher.class);
    private static final String SEPARATOR = "\n\n--------------------\n\n";

    private final List<IMessagePushStrategy> strategies;

    public ResultPublisher(List<IMessagePushStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 发布签到结果
     *
     * @param results 签到结果列表
     */
    public void publish(List<CookieSignResult> results) {
        logger.info("开始推送签到结果");

        PublishContent content = buildContent(results);

        logger.info("签到结果: {}", content.title);
        logger.info("成功率: {}/{}", content.successCount, content.totalCount);

        for (IMessagePushStrategy strategy : strategies) {
            try {
                strategy.pushMessage(content.title, content.message);
            } catch (Exception e) {
                logger.error("推送策略执行失败: {}", e.getMessage(), e);
            }
        }

        logger.info("签到结果推送完成");
    }

    /**
     * 构建发布内容
     */
    private PublishContent buildContent(List<CookieSignResult> results) {
        StringBuilder messageBuilder = new StringBuilder();
        boolean allSuccess = true;
        int successCount = 0;
        int totalCount = results != null ? results.size() : 0;

        if (results == null || results.isEmpty()) {
            messageBuilder.append("未获取到任何签到结果，请检查任务执行情况。\n");
            allSuccess = false;
        } else {
            for (CookieSignResult result : results) {
                SignResultVO signResult = result.getSignResult();
                String userName = signResult.getUserName() != null ?
                        signResult.getUserName() : "未知用户";

                messageBuilder.append(userName).append(":\n")
                        .append(signResult.getFormattedMessage())
                        .append("\n\n耗时: ").append(result.getDuration()).append("ms")
                        .append(SEPARATOR);

                // 判断是否成功
                SignResultCode resultCode = SignResultCode.fromCode(signResult.getCode());
                if (resultCode.isSuccess()) {
                    successCount++;
                } else {
                    allSuccess = false;
                }
            }
        }

        String title = allSuccess ? "自动签到成功" : "自动签到失败";
        messageBuilder.append(String.format("\n签到统计: %d/%d 成功", successCount, totalCount));

        return new PublishContent(title, messageBuilder.toString(), successCount, totalCount);
    }

    /**
     * 发布内容封装类
     */
    private static class PublishContent {
        final String title;
        final String message;
        final int successCount;
        final int totalCount;

        PublishContent(String title, String message, int successCount, int totalCount) {
            this.title = title;
            this.message = message;
            this.successCount = successCount;
            this.totalCount = totalCount;
        }
    }
}
