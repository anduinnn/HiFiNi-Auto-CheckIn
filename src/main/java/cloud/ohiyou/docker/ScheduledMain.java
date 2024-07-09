package cloud.ohiyou.docker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ohiyou
 * @since 2024/7/8 14:11
 */
@SpringBootApplication
@EnableScheduling
public class ScheduledMain {
    public static void main(String[] args) {
        SpringApplication.run(ScheduledMain.class, args);
    }
}
