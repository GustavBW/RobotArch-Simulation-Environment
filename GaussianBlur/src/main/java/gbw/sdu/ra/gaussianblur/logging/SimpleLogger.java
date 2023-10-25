package gbw.sdu.ra.gaussianblur.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SimpleLogger {

    public static final File LOG_DIR = new File("./logs");
    static {
        if(!(LOG_DIR.exists() || LOG_DIR.mkdir())){
            System.out.println("Unable to establish logging dir");
            Runtime.getRuntime().exit(1);
        }
    }
    private static List<String> FILE_BUFFER = Collections.synchronizedList(new ArrayList<>());

    public static String getFormattedNow() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS");
        return dateFormat.format(now);
    }

    public static void dumpToFile(){
        if (FILE_BUFFER.isEmpty()) {
            return; // Nothing to dump
        }

        String fileName = "log_" + getFormattedNow() + ".txt";
        File logFile = new File(LOG_DIR, fileName);

        try (FileWriter fileWriter = new FileWriter(logFile, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            for (String logEntry : FILE_BUFFER) {
                bufferedWriter.write(logEntry);
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        FILE_BUFFER.clear();
    }

    public static void log(String msg){
        FILE_BUFFER.add(getFormattedNow() + ": " + msg);
    }

    public static void logAndExit(String msg, int code){
        log(msg);
        dumpToFile();
        Runtime.getRuntime().exit(code);
    }


}
