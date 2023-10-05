package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWriter {

    private final File dir;
    private final String origin;

    public LogWriter(File loggingDir, String logOrigin) {
        this.dir = loggingDir;
        this.origin = logOrigin;
    }

    public Exception asNewFile(InputStream inputStream) {
        File logFile = new File(dir.getPath() + "/" + getNewFileName());
        try(
                FileOutputStream fileOut = new FileOutputStream(logFile);
                BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(fileOut);
        ){
            String line;
            while((line = bfr.readLine()) != null){
                printWriter.println(line);
                printWriter.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
        return null;
    }

    public ValErr<Instance,Exception> startLoggingInstance(){
        File logFile = new File(dir.getPath() + "/" + getNewFileName());
        return ValErr.encapsulate(()->new Instance(new FileOutputStream(logFile)));
    }

    public static class Instance implements AutoCloseable{
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



    public String getNewFileName(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy_HH_mm_ss");
        String msAsString = System.currentTimeMillis() + "";
        String msNow = msAsString.substring(msAsString.length() - 3);
        return origin + "_" + dtf.format(LocalDateTime.now()) + "_" + msNow;
    }
}
