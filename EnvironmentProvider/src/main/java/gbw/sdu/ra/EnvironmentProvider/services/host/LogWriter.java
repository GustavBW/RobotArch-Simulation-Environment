package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWriter implements ILogWriter {

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

    public ValErr<ILogWriter.Instance,Exception> startLoggingInstance(){
        File logFile = new File(dir.getPath() + "/" + getNewFileName());
        return ValErr.encapsulate(()->new ILogWriter.Instance(new FileOutputStream(logFile)));
    }

    public String getNewFileName(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy_HH_mm_ss");
        String msAsString = System.currentTimeMillis() + "";
        String msNow = msAsString.substring(msAsString.length() - 3);
        return origin + "_" + dtf.format(LocalDateTime.now()) + "_" + msNow;
    }
}
