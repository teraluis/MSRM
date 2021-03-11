package utils;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import core.SchedulerUtils;
import scala.concurrent.ExecutionContext;
import utils.Sage1000Export.Sage1000ExportUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

@Singleton
public class Scheduler {

    // Inject the application's Environment upon start-up and register hook(s) for shut-down.
    @Inject
    public void Scheduler(ActorSystem actorSystem, Config config, ExecutionContext executionContext, UserSynchronization userSynchronization, Sage1000ExportUtils sage1000ExportUtils) {

        // Start optitime collector
        if (config.hasPath("ldap.synchro")) {
            Integer period = config.getInt("ldap.synchro.period");
            Integer hours = config.getInt("ldap.synchro.hours");
            Integer minutes = config.getInt("ldap.synchro.minutes");
            actorSystem.scheduler().schedule(SchedulerUtils.durationToTriggerTime(hours, minutes, period), Duration.ofSeconds(period), userSynchronization::synchronizeUser, executionContext);
        }
        if (config.hasPath("sage1000.export") && config.getBoolean("sage1000.export") && config.hasPath("sftp.server.period") && config.hasPath("sftp.server.hours") && config.hasPath("sftp.server.minutes")) {
            Integer period = config.getInt("sftp.server.period");
            Integer hours = config.getInt("sftp.server.hours");
            Integer minutes = config.getInt("sftp.server.minutes");
            Duration sage1000Time = SchedulerUtils.durationToTriggerTime(hours, minutes, period);
            actorSystem.scheduler().schedule(sage1000Time, Duration.ofSeconds(period), () -> sage1000ExportUtils.export("ADX"), executionContext);
            actorSystem.scheduler().schedule(sage1000Time.plusMinutes(1), Duration.ofSeconds(period), () -> sage1000ExportUtils.parse("ADX"), executionContext);
        }
    }
}
