package gbw.sdu.ra.EnvironmentProvider.services.docker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DockerfileBuilderTest {

    private static final String REFERENCE_FILE_DIR = "./dockerReferenceFiles";
    private File testReferenceFile;
    private String testReferenceFileName = ".example";
    private File tempOutDir;
    private String tempOutDirPath = "./dockerReferenceTestFiles";
    private String tempInDirPath = "./DockerfileBuilderTestDir";
    private File tempInDir;

    // Initialize the temporary reference file directory before each test
    @BeforeEach
    void setUp() {
        // Create the temporary reference file directory if it doesn't exist
        testReferenceFile = new File(REFERENCE_FILE_DIR + "/" + testReferenceFileName);
        if (!testReferenceFile.exists()) {
            fail("Unable to find .example test file in ./dockerReferenceFiles.");
        }
        tempOutDir = new File(tempOutDirPath + System.currentTimeMillis());
        if(!tempOutDir.exists() && !tempOutDir.mkdir()){
            fail("Unable to establish /dockerReferenceTestFiles for testing purposes");
        }
        tempOutDir.deleteOnExit();
        tempInDir = new File(tempInDirPath);
        if(!tempInDir.exists() && !tempInDir.mkdir()){
            fail("Unable to establish temp input dir");
        }
        tempInDir.deleteOnExit();
    }

    // Clean up the temporary reference file directory after each test
    @AfterEach
    void tearDown() {
        // Delete all files in the reference file directory
        File[] files = tempOutDir.listFiles();
        if (files != null) {
            Arrays.stream(files).forEach(File::delete);
        }
        File[] inDirContent = tempInDir.listFiles();
        if(inDirContent != null){
            Arrays.stream(inDirContent).forEach(File::delete);
        }
        // Delete the reference file directory itself
        if (tempOutDir.exists() && !tempOutDir.delete()) {
            fail("Unable to delete the temporary reference file directory.");
        }
    }
    // Helper method to create a reference file with the specified content
    private File createReferenceFile(String fileName, String content) {
        File referenceFile = new File(REFERENCE_FILE_DIR + "/" + fileName);
        try (FileWriter writer = new FileWriter(referenceFile.getPath())) {
            writer.write(content);
        } catch (IOException e) {
            fail("Failed to create the reference file: " + e.getMessage());
        }
        referenceFile.deleteOnExit();
        return referenceFile;
    }
    @Test
    void init() {
    }

    @Test
    void getReferenceFiles() {
        assertTrue(DockerfileBuilder.getReferenceFiles().contains(testReferenceFile));
    }

    @Test
    void buildFromFile_ValidReferenceFile() {
        // Create a valid reference file with content

        // Create a DockerfileBuilder instance
        DockerfileBuilder builder = new DockerfileBuilder();

        // Build from the valid reference file
        Exception error = builder.buildFromFile(testReferenceFile);

        // Ensure no error occurred during the build
        assertNull(error);

        // Verify that the API version was correctly read and set
        assertEquals("vThisIsATestFile", builder.getApiVersion());
        assertEquals(4242, builder.getServerPort());
    }

    @Test
    void buildFromFile_InvalidReferenceFile_MissingApiVersion() {
        // Create an invalid reference file without specifying RA_API_VERSION
        File referenceFile = createReferenceFile("invalid_reference_file.txt", "# Invalid Dockerfile content");

        // Create a DockerfileBuilder instance
        DockerfileBuilder builder = new DockerfileBuilder();

        // Build from the invalid reference file
        Exception error = builder.buildFromFile(referenceFile);

        // Ensure an error occurred due to missing RA_API_VERSION
        assertNotNull(error);
        assertTrue(error.getMessage().contains("RA_API_VERSION not specified."));
    }

    @Test
    void buildFromFile_InvalidReferenceFile_IOError() {
        // Create a reference file in a non-existent directory
        File referenceFile = new File("./non_existent_directory/non_existent_reference_file.txt");

        // Create a DockerfileBuilder instance
        DockerfileBuilder builder = new DockerfileBuilder();

        // Build from the invalid reference file
        Exception error = builder.buildFromFile(referenceFile);

        // Ensure an error occurred due to IO error (file not found)
        assertNotNull(error);
        assertTrue(error instanceof FileNotFoundException);
    }



    @Test
    void testBuildFromFile() {
    }

    @Test
    void processLine() {
    }

    @Test
    void readInternalPort() {
    }

    @Test
    void readApiVersion() {
    }

    @Test
    void fillHostSpecifcEnvVars() {
    }

    @Test
    void fillSpecificationEnvVars() {
    }

    @Test
    void addEnv() {
    }

    @Test
    void saveAndGetPath() {
    }

    @Test
    void getApiVersion() {
    }

    @Test
    void getFileName() {
    }

    @Test
    void getServerPort() {
    }
}