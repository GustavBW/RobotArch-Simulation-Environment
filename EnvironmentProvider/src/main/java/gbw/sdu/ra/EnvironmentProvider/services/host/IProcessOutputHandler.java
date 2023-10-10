package gbw.sdu.ra.EnvironmentProvider.services.host;

import java.io.InputStream;
import java.util.function.Consumer;

public interface IProcessOutputHandler extends Runnable {
    boolean appendPerLineExec(Consumer<String> perLineFunc);
    void setInputStream(InputStream stream);
}
