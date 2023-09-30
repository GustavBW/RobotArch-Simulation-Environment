package gbw.sdu.ra.EnvironmentProvider.services;

import gbw.sdu.ra.EnvironmentProvider.dtos.ServerAction;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import gbw.sdu.ra.EnvironmentProvider.services.functional.Verifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ServerMetadataInspector implements Verifier<ServerMetadata> {
    @Override
    public String verify(ServerMetadata data){
        Map<String, ServerAction> actions = data.actions();
        if(actions.get(ServerAction.START_PROCESS) == null){
            return "Missing action: \""+ServerAction.START_PROCESS+"\"";
        }
        if(actions.get(ServerAction.GET_METADATA) == null){
            return "Missing action: \""+ServerAction.GET_METADATA+"\"";
        }

        //Should probably do a lot more too
        return null;
    }
}
