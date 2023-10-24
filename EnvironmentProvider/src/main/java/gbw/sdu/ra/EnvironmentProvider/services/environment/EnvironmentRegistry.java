package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import gbw.sdu.ra.EnvironmentProvider.services.host.IShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Holds minimal data on environment services
//Aka their /metadata routes
@Service
public class EnvironmentRegistry implements IEnvironmentRegistry{

    private final List<EnvironmentEntry> cloudEntries = Collections.synchronizedList(new ArrayList<>());
    private final RestTemplate template;
    private final IShellService shell;

    @Autowired
    public EnvironmentRegistry(RestTemplate template, IShellService shell){
        this.template = template;
        this.shell = shell;
    }

    @Override
    public ValErr<ServerMetadata,Exception> register(ServerMetadata metadata, String absoluteMetadataUrl){
        EnvironmentEntry entry = addDeployed(getNextId(), absoluteMetadataUrl, metadata.specification());
        System.out.println("EnvironmentRegistry.21, adding remote: " + entry);
        return ValErr.value(metadata);
    }
    @Override
    public int getCloudSize(){
        return cloudEntries.size();
    }


    private static final String[] stopContainersCmd = new String[]{
            "docker", "stop", "$(docker ps -q -a --filter \"label=group1=ra_sim\")"
    };
    private static final String[] removeContainersCmd = new String[]{
            "docker", "rm", "$(docker ps -q -a --filter \"label=group1=ra_sim\")"
    };


    /**
     * @return true on clear, false on no environment
     */
    @Override
    public Exception clear() {
        if(cloudEntries.size() == 0) return new Exception("No environment");
        //do something cool
        for(EnvironmentEntry entry : cloudEntries){
            //send the shutdown notice
        }
        ValErr<Integer, Exception> stopAttempt = shell.execSeqSync(stopContainersCmd);
        if(stopAttempt.hasError()) return stopAttempt.err();
        ValErr<Integer, Exception> removeAttempt = shell.execSeqSync(removeContainersCmd);
        if(removeAttempt.hasError()) return removeAttempt.err();

        return null;
    }

    @Override
    public boolean isEmpty(){
        return cloudEntries.isEmpty();
    }
    @Override
    public List<EnvironmentEntry> entries(){
        return cloudEntries;
    }

    @Override
    public long getNextId() {
        return System.currentTimeMillis();
    }

    /**
     *
     * @param id
     * @param metadataUrl - absolute
     * @param specification
     * @return
     */
    @Override
    public EnvironmentEntry addDeployed(long id, String metadataUrl, ServerSpecification specification) {
        EnvironmentEntry entry = new EnvironmentEntry(id, metadataUrl, specification);
        cloudEntries.add(entry);
        return entry;
    }
}
