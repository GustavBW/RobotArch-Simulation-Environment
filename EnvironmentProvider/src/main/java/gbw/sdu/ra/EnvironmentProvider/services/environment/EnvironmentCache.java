package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.controllers.EnvironmentController;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Holds minimal data on environment services
//Aka their /metadata routes
@Service
public class EnvironmentCache {

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
}
