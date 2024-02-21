package cloud.ohiyou;

/**
 * @author ohiyou
 * @since ${DATE} ${TIME}
 */
public class Main {
    public static void main(String[] args) {
        // 获取名为 COOKIE 的环境变量的值
        String cookieValue = System.getenv("COOKIE");

        if (cookieValue != null) {
            System.out.println("COOKIE 环境变量的值是: " + cookieValue);
        } else {
            System.out.println("COOKIE 环境变量未设置");
        }
    }
}