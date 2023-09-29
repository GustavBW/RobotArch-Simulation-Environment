package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

}
