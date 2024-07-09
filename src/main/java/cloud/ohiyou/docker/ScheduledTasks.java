package cloud.ohiyou.docker;

import cloud.ohiyou.actions.Main;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author ohiyou
 * @since 2024/7/8 14:22
 */
@Component
public class ScheduledTasks {

    @Scheduled(cron = "${scheduled.cron:0 30 6 * * *}")
    public void scheduledTask(){
        System.out.println("任务开始执行: " + LocalDateTime.now());
        Main.main(null);
        System.out.println("任务执行结束: " + LocalDateTime.now());
    }
}
