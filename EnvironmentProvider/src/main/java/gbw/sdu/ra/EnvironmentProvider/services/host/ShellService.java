package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.Ref;
import gbw.sdu.ra.EnvironmentProvider.ValErr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.*;

@Service
public class ShellService {
    private final ExecutorService globalSequentialExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final ForkJoinPool pool = new ForkJoinPool(3);
    private final LogWriter logger;

    @Autowired
    public ShellService(LogWriter logger){
        this.logger = logger;
    }

    /**
     * Called by the HostAccessService
     * @return any fatal error. The program should exit
     */
    Exception init(){
        //Create log dir
        File loggingDir = new File("./shellLogs");
        boolean createLogDir = loggingDir.mkdir();
        if(!(loggingDir.exists() || createLogDir)){
            return new Exception("Unable to create shell logging directory");
        }
        return null;
    }

    /**
     * @param cmd Does not support white-space names - because windows amirite
     * @return Either an error as soon as possible, or the exit code of the process
     * Duly note that exit code 0 means success.
     */
    public ValErr<Integer,Exception> execSeqSync(String[] cmd){
        return execSeqSync(cmd, new ProcessOutputHandler(l -> {}));
    }
    public ValErr<Integer,Exception> execSeqSync(String[] cmd, ProcessOutputHandler gobbler){
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        ValErr<LogWriter.Instance,Exception> startLoggingInstance = logger.startLoggingInstance();

        try (LogWriter.Instance loggingInstance = startLoggingInstance.val()){
            if(startLoggingInstance.hasError()) startLoggingInstance.err().printStackTrace();
            gobbler.appendPerLineExec(loggingInstance::writeLine);

            Process process = processBuilder.start();
            gobbler.setInputStream(process.getInputStream());
            CompletableFuture<Void> handling = CompletableFuture.runAsync(gobbler::start);
            handling.join();

            return ValErr.encapsulate(() -> process.waitFor()); // Return the process exit code
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ValErr.value(-1);
    }



    private static final String[] EMPTY = new String[0];

}
