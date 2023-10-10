package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LogWriterTest {

    @BeforeEach
    void setUp() {
    }

    private final List<VoidFunc> deferList = new ArrayList<>();

    @AfterEach
    void tearDown() {
        deferList.forEach(VoidFunc::apply);
        deferList.clear();
    }

    private File getTempDir(){
        File tempDir = new File("./LogWriterTestDir" + System.currentTimeMillis());
        tempDir.mkdir();
        tempDir.deleteOnExit();
        assertNotNull(tempDir);
        deferList.add(tempDir::delete);
        return tempDir;
    }

    @Test
    void asNewFile() throws IOException{
        // Step 1: Create an InputStream with the data you want to write
        String testData = "This is a test log entry.\nAnother line for testing.\n";
        InputStream inputStream = new ByteArrayInputStream(testData.getBytes());


        // Step 2: Call the asNewFile method of the LogWriter
        File dir = getTempDir(); // Replace with your method to get a temporary directory
        LogWriter logger = new LogWriter(dir, "LogWriterTest");
        Exception exception = logger.asNewFile(inputStream);

        //Step 2.5: Check if a new file was made
        File[] dirContent = dir.listFiles();
        assertNotNull(dirContent);
        assertEquals(1, dirContent.length);
        File logFile = dirContent[0];
        // Step 3: Read the file and check if it contains the expected contents
        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        StringBuilder fileContents = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            fileContents.append(line).append("\n");
        }

        reader.close();

        // Check if the file contents match the expected data
        assertEquals(testData, fileContents.toString());

        // Optionally, check if the asNewFile method returned null (indicating success)
        assertNull(exception);
    }

    @Test
    void startLoggingInstance() throws Exception {
        // Create a LogWriter with a temporary directory
        File dir = getTempDir(); // Replace with your method to get a temporary directory
        LogWriter logger = new LogWriter(dir, "LogWriterTest");

        // Start logging an instance
        ValErr<ILogWriter.Instance, Exception> result = logger.startLoggingInstance();

        // Ensure that no exception occurred during instance creation
        assertNull(result.err());

        // Write some data to the instance
        ILogWriter.Instance instance = result.val();
        instance.writeLine("This is a test log entry.");
        instance.writeLine("Another line for testing.");

        // Close the instance
        instance.close();

        //Find the log file (.startInstance is pretty stateful)
        File[] dirContent = dir.listFiles();
        assertNotNull(dirContent);
        File logFile = dirContent[0];
        assertNotNull(logFile);

        // Verify that the log file was created and contains the expected data
        assertTrue(logFile.exists());

        // Optionally, read the file and check its contents
        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        StringBuilder fileContents = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            fileContents.append(line).append("\n");
        }

        reader.close();

        String expectedData = "This is a test log entry.\nAnother line for testing.\n";
        assertEquals(expectedData, fileContents.toString());
    }

    @FunctionalInterface
    public interface VoidFunc{
        void apply();
    }
}