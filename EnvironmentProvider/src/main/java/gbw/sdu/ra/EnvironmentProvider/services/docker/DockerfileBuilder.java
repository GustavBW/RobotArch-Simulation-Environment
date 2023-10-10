package gbw.sdu.ra.EnvironmentProvider.services.docker;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import gbw.sdu.ra.EnvironmentProvider.services.Timestamped;
import gbw.sdu.ra.EnvironmentProvider.services.host.HostAccessService;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

//Depends on directories $WD/tempDockerfiles & $WD/dockerReferenceFiles
public class DockerfileBuilder implements IDockerfileBuilder {

    private static Timestamped<List<File>> KNOWN_REFERENCE_FILES;
    private final static long CACHE_INVALIDATION_RATE = 30_000; //in ms
    private static String REFERENCE_FILE_DIR = "./dockerReferenceFiles";
    private static String OUTPUT_FILE_DIR = "./deploymentDockerfiles";
    private static String PROVIDER_COMMENT_PREFIX = "@EP";
    private String irn;
    private String filename;
    private String apiVersion;
    private int internalServerPort = 4242;

    private final LinkedHashMap<String,List<String>> linesPerSegmentMap = new LinkedHashMap<>();
    private int latestUnnamedSegment = 0;

    public DockerfileBuilder(){}

    DockerfileBuilder(String testOut, String testIn){
        REFERENCE_FILE_DIR = testIn;
        OUTPUT_FILE_DIR = testOut;
    }

    public static Exception init(){
        File tempDir = new File(OUTPUT_FILE_DIR);
        if(!(tempDir.exists() || tempDir.mkdir())) return new IOException("Unable to create WD/tempDockerfiles directory");
        File refDir = new File(REFERENCE_FILE_DIR);
        if(!(refDir.exists() || refDir.mkdir())) return new IOException("Unable to create WD/dockerReferenceFiles directory");
        return null;
    }

    public static List<File> getReferenceFiles(){
        //A sprinkle of cache invalidation to allow to update... sometimes.
        if(KNOWN_REFERENCE_FILES == null || System.currentTimeMillis() - KNOWN_REFERENCE_FILES.timestamp() > CACHE_INVALIDATION_RATE){
            File refDir = new File(REFERENCE_FILE_DIR);
            File[] files = refDir.listFiles();
            KNOWN_REFERENCE_FILES = Timestamped.now(Arrays.asList(files));
        }
        return KNOWN_REFERENCE_FILES.data();
    }
    @Override
    public Exception buildFromFile(String referenceFileName){
        File referenceFile = new File(REFERENCE_FILE_DIR + "/" +referenceFileName);
        return buildFromFile(referenceFile);
    }
    @Override
    public Exception buildFromFile(File referenceFile){
        try(BufferedReader reader = new BufferedReader(new FileReader(referenceFile))){
            Exception[] err = new Exception[1];
            reader.lines()
                    .filter(line -> !line.startsWith("#"))
                    .filter(line -> !line.trim().isBlank())
                    .forEach(line -> processLine(line, err));

            if(err[0] == null && apiVersion == null){
                err[0] = new Exception("RA_API_VERSION not specified.");
            }
            return err[0];
        } catch (IOException e){
            return e;
        }
    }

    private Map<String,Function<String,Exception>> onEnvHandlers = Map.of(
              "RA_API_VERSION", this::readApiVersion,
            "RA_INTERNAL_SERVER_PORT", this::readInternalPort
    );
    private void processLine(String line, Exception[] err) {
        if(!line.startsWith("<slot")){
            linesPerSegmentMap.computeIfAbsent(latestUnnamedSegment + "", k -> new ArrayList<>()).add(line);
            if(line.startsWith("ENV")){
                line = line.substring(4);
                String[] kv = line.split("=");
                Function<String,Exception> handler = onEnvHandlers.get(kv[0]);
                if(handler == null) return;
                Exception error = handler.apply(kv[1]);
                if(error != null) {
                    err[0] = error;
                }
            }
        }else{
            Exception slotErr = addSlot(line);
            if(err != null) {
                err[0] = slotErr;
            }
            latestUnnamedSegment++;
        }
    }

    Exception readInternalPort(String value){
        if(value.isBlank()) return null;
        String secondHalfCleaned = value.replaceAll("\"","").trim();
        this.internalServerPort = Integer.parseInt(secondHalfCleaned);
        return null;
    }

    Exception readApiVersion(String value){
        if(value.isBlank()) return new Exception("RA_API_VERSION not specified.");
        String secondHalfCleaned = value.replaceAll("\"","").trim();
        if(!secondHalfCleaned.contains("v")) return new Exception("RA_API_VERSION should be stated as \"v\"<number>");
        this.apiVersion = secondHalfCleaned;
        return null;
    }

    private Exception addSlot(String line){
        final int indexOfSlotName = line.indexOf("name=\"");
        if(indexOfSlotName == -1){ //unnamed slot error
            return new Exception("Unnamed slot, a <slot ... /> must contain name=\"<something>\"");
        }
        final int indexOfNameStart = indexOfSlotName + "name=\"".length();
        final int indexOfNameEnd = line.indexOf("\"", indexOfNameStart);
        String slotName = line.substring(indexOfNameStart,indexOfNameEnd);
        linesPerSegmentMap.put(slotName, new ArrayList<>());
        return null;
    }
    @Override
    public void fillHostSpecifcEnvVars(long environmentId, int port, String hostIpv4){
        addComment("env","Following host specific env vars have been set by the EnvironmentProvider");
        addEnv("RA_CONTAINER_EXTERNAL_PORT", port + "");
        addEnv("RA_ENVIRONMENT_ID", environmentId + "");
        addEnv("RA_CONTAINER_HOST_IP", hostIpv4);
        addComment("env", "insertion end");
    }
    @Override
    public void fillSpecificationEnvVars(ServerSpecification specification){
        this.irn = specification.irn(); //Stored for generating filename
        addComment("env", "Following ServerSpecification env vars have been set by the EnvironmentProvider");
        addEnv("RA_STATIC_LATENCY",specification.latency() + "");
        addEnv("RA_CPUS", specification.cpus() + "");
        addEnv("RA_MEMORY", specification.memory() + "");
        addEnv("RA_IRN", irn);
        addComment("env", "insertion end");
    }
    @Override
    public void addEnv(String key, String value){
        linesPerSegmentMap.computeIfAbsent("env", k -> new ArrayList<>())
                .add("ENV " + key + "=" + value);
    }
    @Override
    public void addComment(String segmentKey, String comment){
        linesPerSegmentMap.computeIfAbsent(segmentKey, k -> new ArrayList<>())
                .add("#" + PROVIDER_COMMENT_PREFIX + " " + comment);
    }
    @Override
    public ValErr<String,Exception> saveAndGetPath(){
        filename = irn + "_" + System.currentTimeMillis();
        File actualFile = new File(OUTPUT_FILE_DIR+"/"+filename);

        try (FileWriter writer = new FileWriter(actualFile)){
            for(String line : render().toList()) {
                writer.write(line + "\n");
            }
        }catch (IOException e){
            return ValErr.error(e);
        }
        return ValErr.encapsulate(actualFile::getCanonicalPath);
    }
    @Override
    public String getApiVersion(){
        return apiVersion;
    }

    @Override
    public String getFileName(){
        return filename;
    }

    @Override
    public int getServerPort(){
        return internalServerPort;
    }

    private Stream<String> render(){
        return linesPerSegmentMap.values()
                .stream()
                .flatMap(Collection::stream);
    }
    Map<String,List<String>> __getFileMap(){
        return linesPerSegmentMap;
    }

}
