package gbw.sdu.ra.EnvironmentProvider.services.docker;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.services.host.HostAccessService;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

//Depends on directories $WD/tempDockerfiles & $WD/dockerReferenceFiles
public class DockerfileBuilder {

    public static List<File> KNOWN_REFERENCE_FILES;
    private String irn;
    private int port;
    private final LinkedHashMap<String,List<String>> linesPerSegmentMap = new LinkedHashMap<>();
    private int latestUnnamedSegment = 0;

    public static Exception init(){
        File tempDir = new File("./tempDockerfiles");
        if(!(tempDir.exists() || tempDir.mkdir())) return new IOException("Unable to create WD/tempDockerfiles directory");
        File refDir = new File("./dockerReferenceFiles");
        if(!(refDir.exists() || refDir.mkdir())) return new IOException("Unable to create WD/dockerReferenceFiles directory");
        return null;
    }

    public static List<File> getReferenceFiles(){
        if(KNOWN_REFERENCE_FILES == null){
            File refDir = new File("./dockerReferenceFiles");
            File[] files = refDir.listFiles();
            KNOWN_REFERENCE_FILES = Arrays.asList(files);
        }
        return KNOWN_REFERENCE_FILES;
    }

    public Exception buildFromFile(String referenceFileName){
        try(BufferedReader reader = new BufferedReader(new FileReader("./dockerReferenceFiles/"+referenceFileName))){
            Exception[] slotErr = new Exception[1];
            reader.lines()
                    .filter(line -> line.startsWith("#"))
                    .forEach(
                        line -> {
                            if(!lineContainsSlot(line)){
                                linesPerSegmentMap.computeIfAbsent(latestUnnamedSegment + "", k -> new ArrayList<>()).add(line);
                            }else{
                                slotErr[0] = addSlot(line);
                                latestUnnamedSegment++;
                            }
                        }
                    );
            return slotErr[0];
        } catch (IOException e){
            return e;
        }
    }

    private boolean lineContainsSlot(String line){
        return line.startsWith("<slot");
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


    public Exception fillHostSpecifcEnvVars(HostAccessService hostService, long environmentId, String irn){
        ValErr<String,Exception> hostIp = hostService.getIpv4();
        if(hostIp.hasError()) return hostIp.err();
        ValErr<Integer,Exception> availablePort = hostService.getAvailablePort();
        if(availablePort.hasError()) return availablePort.err();

        this.irn = irn; //Stored for generating filename
        this.port = availablePort.val();
        //RA_IRN
        addEnv("RA_CONTAINER_EXTERNAL_PORT", port + "");
        addEnv("RA_ENVIRONMENT_ID", environmentId + "");
        addEnv("RA_IRN", irn);
        addEnv("RA_CONTAINER_HOST_IP", hostIp.val());
        return null;
    }

    public void addEnv(String key, String value){
        linesPerSegmentMap.computeIfAbsent("env", k -> new ArrayList<>()).add("ENV " + key + "=" + value);
    }

    public ValErr<String,Exception> saveAndGetPath(){
        final String filename = irn + "_" + System.currentTimeMillis();
        File actualFile = new File("./tempDockerfiles/"+filename);
        try (FileWriter writer = new FileWriter(actualFile.getPath())){
            for(String line : render().toList()) {
                writer.write(line + "\n");
            }
        }catch (IOException e){
            return ValErr.error(e);
        }
        return ValErr.encapsulate(actualFile::getCanonicalPath);
    }

    private Stream<String> render(){
        return linesPerSegmentMap.values()
                .stream()
                .flatMap(Collection::stream);
    }

}
