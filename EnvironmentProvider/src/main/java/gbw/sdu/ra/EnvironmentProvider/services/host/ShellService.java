package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
public class ShellService {
    private static class StreamGobbler implements Runnable {
        //thx: https://www.baeldung.com/run-shell-command-in-java
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
    private final ExecutorService globalSequentialExecutor = Executors.newSingleThreadExecutor();

    private Process dockerShell;

    /**
     * Called by the HostAccessService
     * @return any fatal error. The program should exit
     */
    Exception init(){
        //Create log dir
        boolean createLogDir = new File("./shellLogs").mkdir();
        if(!createLogDir){
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
        final Process process;
        try{
            process = Runtime.getRuntime().exec(cmd);
        }catch (IOException e){
            return ValErr.error(e);
        }

        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        globalSequentialExecutor.submit(streamGobbler);
        return ValErr.encapsulate(() -> process.waitFor());
    }



    private static final String[] EMPTY = new String[0];

}
