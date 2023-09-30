package gbw.sdu.ra.EnvironmentProvider.services;

import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import gbw.sdu.ra.EnvironmentProvider.services.functional.Verifier;
import org.springframework.stereotype.Service;

@Service
public class ServerSpecInspector implements Verifier<ServerSpecification> {

    @Override
    public String verify(ServerSpecification specification){
        return null;
    }
}
