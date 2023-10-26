package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.Ref;
import gbw.sdu.ra.EnvironmentProvider.ValErr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.*;

@Service
public class ShellService implements IShellService {
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
        if(!(loggingDir.exists() || loggingDir.mkdir())){
            return new Exception("Unable to create shell logging directory");
        }
        File[] loggingDirContent = loggingDir.listFiles();
        if(loggingDirContent != null){
            for(File f : loggingDirContent){
                f.delete();
            }
        }
        return null;
    }



    @Override
    public ValErr<Integer,Exception> execSeqSync(String[] cmd){
        return execSeqSync(cmd, new ProcessOutputHandler(l -> {}));
    }
    @Override
    public ValErr<Integer,Exception> execSeqSync(String[] cmd, ProcessOutputHandler gobbler){
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        ValErr<ILogWriter.Instance,Exception> startLoggingInstance = logger.startLoggingInstance();

        try (ILogWriter.Instance loggingInstance = startLoggingInstance.val()){
            if(startLoggingInstance.hasError()) startLoggingInstance.err().printStackTrace();
            gobbler.appendPerLineExec(loggingInstance::writeLine);

            loggingInstance.writeLine("[INSERTED BY ENVIRONMENT PROVIDER v]");
            loggingInstance.writeLine(Arrays.toString(cmd));
            loggingInstance.writeLine("[END OF INSERT ^]");

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

}
