package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShellServiceTest {


    private final List<VoidFunc> deferList = new ArrayList<>();

    @Test
    void init() {
        ShellService service = new ShellService(new NOOPLogWriter());
        //expect it to succeed regardless
        assertNull(service.init());
    }

    /**
     * NB: WINDOWS NATIVE. WONT WORK ON LINUX OR MACOS
     */
    private final String[] testCmd = new String[]{"cmd.exe","/c","echo","hello there"};

    @Test
    void execSeqSync() {
        ShellService service = new ShellService(new NOOPLogWriter());
        //Expect it to succeed
        ValErr<Integer,Exception> result = service.execSeqSync(testCmd);
        if(result.hasError()) result.err().printStackTrace();
        //with no errors
        assertFalse(result.hasError());
        //and an exit code of 0
        assertEquals(0,result.val());
    }

    @Test
    void testWithStreamGobbler() {
        ShellService service = new ShellService(new NOOPLogWriter());
        List<String> buffer = new ArrayList<>();
        //Expect it to succeed
        ValErr<Integer,Exception> result = service.execSeqSync(testCmd, new ProcessOutputHandler(buffer::add));
        if(result.hasError()) result.err().printStackTrace();
        assertFalse(result.hasError());
        assertEquals(0,result.val());

        //We should have but 1 line in the buffer, i.e. what was echoed
        assertEquals(1, buffer.size());
        //And that line should be "hello there"
        assertEquals("\"hello there\"", buffer.get(0));
    }

    @Test
    void testWithLogging(){
        //Create a temp testing directory
        File tempDir = new File("./ShellServiceTestLogs" + System.currentTimeMillis());
        assertTrue(tempDir.mkdir());
        //defer(tempDir::delete);

        LogWriter logWriter = new LogWriter(tempDir, "ShellServiceTest");
        ShellService service = new ShellService(logWriter);
        //Expect it to succeed
        ValErr<Integer,Exception> result = service.execSeqSync(testCmd);
        if(result.hasError()) result.err().printStackTrace();
        assertFalse(result.hasError());
        assertEquals(0,result.val());

        File[] contentsOfTempLogDir = tempDir.listFiles();
        assertNotNull(contentsOfTempLogDir);
        assertEquals(1,contentsOfTempLogDir.length);
        ValErr<List<String>,Exception> readLoggedFile = ValErr.encapsulate(() -> Files.readAllLines(Paths.get(contentsOfTempLogDir[0].getPath())));
        if(readLoggedFile.hasError()) result.err().printStackTrace();
        assertFalse(readLoggedFile.hasError());
        List<String> fileContents = readLoggedFile.val();
        assertNotNull(fileContents);

    }

    @AfterEach
    void execDefer(){
        deferList.forEach(VoidFunc::apply);
        deferList.clear();
    }
    void defer(VoidFunc func){
        deferList.add(func);
    }

    private static class NOOPLogWriter extends LogWriter {
        public NOOPLogWriter() {
            super(null,null);
        }
        @Override
        public Exception asNewFile(InputStream stream){
            return null;
        }

    }
    @FunctionalInterface
    private interface VoidFunc{
        void apply();
    }
}