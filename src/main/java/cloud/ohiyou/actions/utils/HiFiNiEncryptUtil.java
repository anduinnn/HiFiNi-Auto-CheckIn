package cloud.ohiyou.actions.utils;

/**
 * @author ohiyou
 * @since 2024/3/18 9:15
 */
public class HiFiNiEncryptUtil {
    /**
     *  待加密的字符串
     */
//    public static final String SIGN = "729a5b06332b6a6a8b1f2841730af3343d9bd871be8dd631f83cbe9dcdc6b48b";

    /**
     * 生成动态密钥的函数
     * @return String
     */
    public static String generateDynamicKey() {
        // 获取当前时间的时间戳（毫秒）
        long currentTime = System.currentTimeMillis();
        // 每5分钟变化一次，总共5个密钥轮换
        int keyIndex = (int) ((currentTime / (5 * 60 * 1000)) % 5);
        String[] keys = {"HIFINI", "HIFINI_COM", "HIFINI.COM", "HIFINI-COM", "HIFINICOM"};
        return keys[keyIndex];
    }

    /**
     *  简单的异或加密函数
     * @param input input
     * @param key key
     * @return String
     */
    public static String simpleEncrypt(String input, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char inputChar = input.charAt(i);
            char keyChar = key.charAt(i % key.length());
            result.append((char) (inputChar ^ keyChar));
        }
        return result.toString();
    }

}
