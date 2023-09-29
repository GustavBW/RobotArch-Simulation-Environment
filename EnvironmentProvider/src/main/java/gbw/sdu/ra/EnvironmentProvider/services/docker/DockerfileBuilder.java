package gbw.sdu.ra.EnvironmentProvider.services.docker;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.services.host.HostInformationService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class DockerfileBuilder {

    private final List<String> memFile = new ArrayList<>();
    private String filename;
    private String irn;

    public ValErr<Stream<String>,Exception> buildFromFile(String referenceFileName){
        try(BufferedReader reader = new BufferedReader(new FileReader("./"+referenceFileName))){
            return ValErr.value(reader.lines());
        }catch (FileNotFoundException e){
            return ValErr.error(e);
        }catch (IOException e){
            return ValErr.error(e);
        }
    }

    public Exception fillHostSpecifcEnvVars(HostInformationService hostService, long environmentId, String irn){
        ValErr<String,Exception> hostIp = hostService.getIpv4();
        if(hostIp.hasError()) return hostIp.err();
        ValErr<Integer,Exception> availablePort = hostService.getAvailablePort();
        if(availablePort.hasError()) return availablePort.err();

        this.irn = irn; //Stored for generating filename

        addEnv("RA_CONTAINER_EXTERNAL_PORT", availablePort.val().toString());
        addEnv("RA_ENVIRONMENT_ID", environmentId + "");
        addEnv("RA_IRN", irn);
        addEnv("RA_CONTAINER_HOST_IP", hostIp.val());
        return null;
    }

    public void addEnv(String key, String value){
        memFile.add("ENV " + key + "=" + value);
    }

    public ValErr<String,Exception> saveAndGetPath(){
        final String filename = irn + "_" + UUID.randomUUID();
        File actualFile = new File("./tempDockerfiles/"+filename);
        try (FileWriter writer = new FileWriter(actualFile.getPath())){
            for(String line : memFile){
                writer.write(line);
            }
        }catch (IOException e){
            return ValErr.error(e);
        }
        return ValErr.encapsulate(actualFile::getCanonicalPath);
    }

}
