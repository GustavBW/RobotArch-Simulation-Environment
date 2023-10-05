package gbw.sdu.ra.EnvironmentProvider.services.docker;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DockerfileBuilderTest {

    private static final String REFERENCE_FILE_DIR = "./dockerReferenceFiles";
    private File exampleFile;
    private String testReferenceFileName = ".example";
    private File tempOutDir;
    private String tempOutDirPath = "./dockerTestOutDir";
    private String tempInDirPath = "./dockerfileBuilderTestDir";
    private File tempInDir;

    // Initialize the temporary reference file directory before each test
    @BeforeEach
    void setUp() throws IOException {
        // Create the temporary reference file directory if it doesn't exist
        exampleFile = new File(REFERENCE_FILE_DIR + "/" + testReferenceFileName);
        if (!exampleFile.exists()) {
            fail("Unable to find .example test file in ./dockerReferenceFiles.");
        }
        tempOutDir = new File(tempOutDirPath + System.currentTimeMillis());
        tempOutDir.createNewFile();
        tempOutDir.mkdir();
        if(!tempOutDir.exists()){
            fail("Unable to establish /dockerTestOutDir for testing purposes");
        }
        tempOutDir.deleteOnExit();
        tempInDir = new File(tempInDirPath);
        tempInDir.createNewFile();
        tempInDir.mkdir();
        if(!tempInDir.exists()){
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
        assertTrue(DockerfileBuilder.getReferenceFiles().contains(exampleFile));
    }

    @Test
    void buildFromFile_ValidReferenceFile() {
        // Create a valid reference file with content

        // Create a DockerfileBuilder instance
        DockerfileBuilder builder = new DockerfileBuilder();

        // Build from the valid reference file
        Exception error = builder.buildFromFile(exampleFile);

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
    void contentReadTest(){
        DockerfileBuilder builder = new DockerfileBuilder();
        Exception err = builder.buildFromFile(exampleFile);
        assertNull(err);
        Map<String, List<String>> segments = builder.__getFileMap();
        //It should pick up on the slots
        List<String> expectedKeys = List.of("dependencies", "env", "build", "exec");
        for(String expected : expectedKeys){
            System.out.println("Expecting key: " + expected);
            List<String> value = segments.get(expected);
            assertNotNull(value);
            assertEquals(0, value.size());
        }

        //There should be an unnamed segment between each slot-based segment
        Collection<List<String>> allSegments = segments.values();
        assertTrue(allSegments.size() > expectedKeys.size());
        //Specifically for the test file there should be 4 unnamed and 4 named segments
        allSegments.forEach(System.out::println);
        assertEquals(8,allSegments.size());
    }
    @Test
    void fillHostSpecifcEnvVars() {
        DockerfileBuilder builder = new DockerfileBuilder();
        //The following values should be formatted and filled in as ENV statements
        builder.fillHostSpecifcEnvVars(69,69,"here");
        List<String> envSegment = builder.__getFileMap().get("env");
        assertNotNull(envSegment);
        envSegment = envSegment.stream().filter(line -> !line.startsWith("#")).toList();
        assertEquals("ENV RA_CONTAINER_EXTERNAL_PORT=69",envSegment.get(0));
        assertEquals("ENV RA_ENVIRONMENT_ID=69",envSegment.get(1));
        assertEquals("ENV RA_CONTAINER_HOST_IP=here",envSegment.get(2));
    }

    @Test
    void fillSpecificationEnvVars() {
        ServerSpecification spec = new ServerSpecification("test",32098,32,52,2573);
        DockerfileBuilder builder = new DockerfileBuilder();
        builder.fillSpecificationEnvVars(spec);
        List<String> envSegment = builder.__getFileMap().get("env");
        assertNotNull(envSegment);
        envSegment = envSegment.stream().filter(line -> !line.startsWith("#")).toList();
        //With only the specification-based environment variables set, they should be first in this segment
        assertEquals("ENV RA_STATIC_LATENCY="+spec.latency(), envSegment.get(0));
        assertEquals("ENV RA_CPUS="+spec.cpus(), envSegment.get(1));
        assertEquals("ENV RA_MEMORY="+spec.memory(), envSegment.get(2));
        assertEquals("ENV RA_IRN="+spec.irn(), envSegment.get(3));
    }

    @Test
    void saveAndGetPath() {
        //The whole ordeal. Lets go
        DockerfileBuilder builder = new DockerfileBuilder(tempOutDirPath,REFERENCE_FILE_DIR);
        Exception err = builder.buildFromFile(exampleFile);
        assertNull(err);

        ServerSpecification spec = new ServerSpecification("test",32098,32,52,2573);
        builder.fillSpecificationEnvVars(spec);
        builder.fillHostSpecifcEnvVars(69,69,"here");

        ValErr<String, Exception> saveAttempt = builder.saveAndGetPath();
        if(saveAttempt.hasError()) saveAttempt.err().printStackTrace();
        assertFalse(saveAttempt.hasError());
        String path = saveAttempt.val();
        assertNotNull(path);


    }

    @Test
    void getFileName() {
    }

}