package gbw.sdu.ra.EnvironmentProvider.services;

import gbw.sdu.ra.EnvironmentProvider.dtos.ServerAction;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import gbw.sdu.ra.EnvironmentProvider.services.functional.Verifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ServerMetadataInspector implements Verifier<ServerMetadata> {
    @Override
    public String verify(ServerMetadata data){
        Map<String, ServerAction> actions = data.actions();
        for(ServerAction.Known expectedAction : ServerAction.Known.values()){
            if(!actions.containsKey(expectedAction.value)){
                return "Missing action: \"" + expectedAction.value + "\"";
            }
        }

        return null;
    }
}
