package gbw.sdu.ra.EnvironmentProvider.services.host;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ProcessOutputHandler extends Thread {

    @FunctionalInterface
    private interface VoidFunction{
        void apply();
    }

    private InputStream inputStream;

    private final VoidFunction onRunDo;
    private final List<Consumer<String>> perLineExec = Collections.synchronizedList(new ArrayList<>());
    private final AtomicBoolean isRunning = new AtomicBoolean(false);


    public ProcessOutputHandler(Consumer<String> consoomer){
        perLineExec.add(consoomer);
        onRunDo = () -> {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(line -> perLineExec
                            .forEach(func -> func.accept(line))
                    );
        };
    }
    @Override
    public void run() {
        isRunning.set(true);
        onRunDo.apply();
        isRunning.set(false);
    }

    /**
     *
     * @return False if its currently in use and thus cannot accept new functions
     */
    public synchronized boolean appendPerLineExec(Consumer<String> perLineFunc){
        if(isRunning.get()) return false;
        perLineExec.add(perLineFunc);
        return true;
    }


    public void setInputStream(InputStream stream){
        this.inputStream = stream;
    }


}
