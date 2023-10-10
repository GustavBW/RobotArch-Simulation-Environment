package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

public interface ILogWriter {

    Exception asNewFile(InputStream inputStream);

    ValErr<Instance,Exception> startLoggingInstance();

    class Instance implements AutoCloseable{
        private final PrintWriter writer;
        private final FileOutputStream outStream;
        public Instance(FileOutputStream outStream){
            this.writer = new PrintWriter(outStream);
            this.outStream = outStream;
        }
        public void writeLine(String line){
            writer.println(line);
            writer.flush();
        }

        @Override
        public void close() throws Exception {
            writer.close();
            outStream.flush();
            outStream.close();
        }
    }
}
