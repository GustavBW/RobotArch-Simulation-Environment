package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.*;

@Service
public class ShellService {
    private final ExecutorService globalSequentialExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final ForkJoinPool pool = new ForkJoinPool(2);
    private LogWriter logger;

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
     *
     * @param cmd Does not support white-space names - because windows amirite
     * @return Either an error as soon as possible, or the exit code of the process
     */
    public ValErr<Integer,Exception> execSeqSync(String[] cmd){
        return execSeqSync(cmd, ProcessOutputHandler.NOOP);
    }
    public ValErr<Integer,Exception> execSeqSync(String[] cmd, ProcessOutputHandler gobbler){
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        ValErr<Process,Exception> run = ValErr.encapsulate(processBuilder::start);
        if(run.hasError()) return ValErr.error(run.err());

        gobbler.setInputStream(run.val().getInputStream());
        CompletableFuture<Void> handling = CompletableFuture.supplyAsync(gobbler, pool);
        Future<?> handling = globalSequentialExecutor.submit(gobbler);
        Future<Exception> logging = globalSequentialExecutor.submit(() -> logger.asNewFile(run.val().getInputStream()));


        return ValErr.encapsulate(() -> run.val().waitFor());
    }



    private static final String[] EMPTY = new String[0];

}
