package gbw.sdu.ra.EnvironmentProvider.configurations;

import gbw.sdu.ra.EnvironmentProvider.services.host.LogWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ShellServiceLogWriter {

    @Bean
    public LogWriter getShellLogWriter(){
        File loggingDir = new File("./shellLogs");
        boolean createLogDir = loggingDir.mkdir();
        return new LogWriter(loggingDir, "ShellService");
    }
}
