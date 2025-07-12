package cloud.ohiyou.vo;

import lombok.Data;

/**
 * @author ohiyou
 * 2025/7/12 16:02
 */
@Data
public class UserInfoVO {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 金币数量
     */
    private Integer coins;
    /**
     * 连续签到天数
     */
    private Integer signStreak;
}
