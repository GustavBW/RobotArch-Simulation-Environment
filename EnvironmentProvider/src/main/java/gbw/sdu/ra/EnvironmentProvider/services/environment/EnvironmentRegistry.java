package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//Holds minimal data on environment services
//Aka their /metadata routes
@Service
public class EnvironmentRegistry {

    private final List<EnvironmentEntry> cloudEntries = new ArrayList<>();


    public ValErr<ServerMetadata,Exception> register(ServerMetadata metadata){
        return ValErr.error(new Exception("Not implemented!"));
    }


    public int getCloudSize(){
        return cloudEntries.size();
    }


    /**
     *
     * @return true on clear, false on no environment
     */
    public boolean clear() {
        if(cloudEntries.size() == 0) return false;
        //do something cool
        return true;
    }


    public boolean isEmpty(){
        return cloudEntries.isEmpty();
    }
    public List<EnvironmentEntry> entries(){
        return cloudEntries;
    }

    public long getNextId() {
        return getCloudSize() + 1;
    }

    public EnvironmentEntry addDeployed(long id, String metadataUrl, ServerSpecification specification) {
        EnvironmentEntry entry = new EnvironmentEntry(id, metadataUrl, specification);
        cloudEntries.add(entry);
        return entry;
    }
}
