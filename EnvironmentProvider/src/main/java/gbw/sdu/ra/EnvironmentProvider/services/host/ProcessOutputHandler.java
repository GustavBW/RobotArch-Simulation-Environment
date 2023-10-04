package gbw.sdu.ra.EnvironmentProvider.services.host;

import java.io.*;
import java.util.function.Consumer;

public class ProcessOutputHandler extends Thread {

    @FunctionalInterface
    private interface VoidFunction{
        void apply();
    }

    private InputStream inputStream;

    public static ProcessOutputHandler NOOP = new ProcessOutputHandler(OutputStream.nullOutputStream());

    private final VoidFunction onRunDo;


    public ProcessOutputHandler(OutputStream outputStream) {
        onRunDo = () -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter writer = new PrintWriter(outputStream);

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
    public ProcessOutputHandler(Consumer<String> consoomer){
        onRunDo = () -> {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consoomer);
        };
    }

    public void setInputStream(InputStream stream){
        this.inputStream = stream;
    }

    @Override
    public void run() {
        onRunDo.apply();
    }
}
