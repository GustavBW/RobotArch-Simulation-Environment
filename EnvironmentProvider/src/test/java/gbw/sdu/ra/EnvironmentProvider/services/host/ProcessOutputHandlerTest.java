package gbw.sdu.ra.EnvironmentProvider.services.host;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProcessOutputHandlerTest {

    @Test
    void testSingleConsumer() throws InterruptedException {
        // Create a ProcessOutputHandler with a single consumer
        AtomicInteger count = new AtomicInteger(0);
        ProcessOutputHandler handler = new ProcessOutputHandler(line -> count.incrementAndGet());

        // Set the input stream with some test data
        String testData = "Line 1\nLine 2\nLine 3\n";
        InputStream inputStream = new ByteArrayInputStream(testData.getBytes());
        handler.setInputStream(inputStream);

        // Start the handler thread and wait for it to finish
        handler.start();
        handler.join();

        // Ensure that the consumer was called for each line
        assertEquals(3, count.get());
    }

    @Test
    void testMultipleConsumers() throws InterruptedException {
        // Create a ProcessOutputHandler with multiple consumers
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);
        ProcessOutputHandler handler = new ProcessOutputHandler(line -> count1.incrementAndGet());

        // Set the input stream with some test data
        String testData = "Line 1\nLine 2\nLine 3\n";
        InputStream inputStream = new ByteArrayInputStream(testData.getBytes());
        handler.setInputStream(inputStream);

        // Append another consumer
        handler.appendPerLineExec(line -> count2.incrementAndGet());

        // Start the handler thread and wait for it to finish
        handler.start();
        handler.join();

        // Ensure that both consumers were called for each line
        assertEquals(3, count1.get());
        assertEquals(3, count2.get());
    }

    @Test
    void testAppendPerLineExecWhileRunning() throws InterruptedException {
        // Create a ProcessOutputHandler
        ProcessOutputHandler handler = new ProcessOutputHandler(line -> {
            try{    //Creating artificial compute time, this'd be done in an instant otherwise
                Thread.sleep(1000);
            }catch(Exception ignored){}
        });

        // Set the input stream with some test data
        String testData = "Line 1\nLine 2\nLine 3\n";
        InputStream inputStream = new ByteArrayInputStream(testData.getBytes());
        handler.setInputStream(inputStream);

        // Start the handler thread
        handler.start();
        //Wait a second for the handler thread to be scheduled
        Thread.sleep(1000);

        // Try to append a new consumer while it's running (should return false)
        assertFalse(handler.appendPerLineExec(line -> {}));

        // Wait for the handler thread to finish
        handler.join();
    }
}