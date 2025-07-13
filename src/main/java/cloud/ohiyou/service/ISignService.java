package cloud.ohiyou.service;

import cloud.ohiyou.vo.SignResultVO;
import cloud.ohiyou.vo.UserInfoVO;

/**
 * 签到服务接口
 *
 * @author ohiyou
 * 2025/7/12 15:07
 */
public interface ISignService {

    /**
     * 签到
     *
     * @param cookie cookie
     * @return 签到结果
     */
    SignResultVO signIn(String cookie);

    /**
     * 获取用户信息
     *
     * @param cookie cookie
     * @return 用户信息
     */
    UserInfoVO getUserInfo(String cookie);

    /**
     * 获取连续签到天数
     *
     * @param cookie 用户cookie
     * @return 连续签到天数
     */
    Integer getSignStreak(String cookie);


    /**
     * 执行完整的签到流程
     *
     * @param cookie cookie
     * @return 完整的签到结果
     */
    default SignResultVO signInWithUserInfo(String cookie) {
        SignResultVO signResult = signIn(cookie);
        if (signResult.getCode() == 0 || signResult.getCode() == -1) {  // 签到成功才获取用户信息
            try {
                UserInfoVO userInfo = getUserInfo(cookie);
                if (userInfo.getUserName() != null) {
                    signResult.setUserName(userInfo.getUserName());
                }
                // 获取连续签到天数
                Integer signStreak = getSignStreak(cookie);
                if (signStreak != null) {
                    userInfo.setSignStreak(signStreak);
                }
                signResult.setUserInfo(userInfo);
            } catch (Exception e) {
                System.out.println("获取用户信息失败: " + e.getMessage());
            }
        }
        return signResult;
    }
}
