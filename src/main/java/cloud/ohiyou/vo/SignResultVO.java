package cloud.ohiyou.vo;

import lombok.Data;

/**
 * @author ohiyou
 * @since 2024/2/21 11:37
 */
@Data
public class SignResultVO {
    /**
     * 返回code
     */
    private Integer code;
    /**
     * 返回信息
     */
    private String message;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户信息
     */
    private UserInfoVO userInfo;

    public SignResultVO(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取格式化的签到结果消息
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();

        // 根据code显示不同的结果图标
        if (code != null && code == 1) {
            sb.append(message);  // 签到成功
        } else if (code != null && code == -1) {
            sb.append(message);  // 今日已签到
        } else {
            sb.append(message);  // 其他错误
        }

        if (userInfo != null) {
            if (userInfo.getCoins() != null) {
                sb.append("\n\n当前金币: ").append(userInfo.getCoins());
            }
            if (userInfo.getSignStreak() != null) {
                sb.append("\n\n连续签到: 第").append(userInfo.getSignStreak()).append("天");
            }
        }

        return sb.toString();
    }

}
