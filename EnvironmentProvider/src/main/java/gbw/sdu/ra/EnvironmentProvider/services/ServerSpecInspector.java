package gbw.sdu.ra.EnvironmentProvider.services;

import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import gbw.sdu.ra.EnvironmentProvider.services.functional.Verifier;
import org.springframework.stereotype.Service;

@Service
public class ServerSpecInspector implements Verifier<ServerSpecification> {

    @Override
    public String verify(ServerSpecification specification){
        if(specification.cpus() <= 0) return "CPU Shares cannot be less than or equal to 0";
        if(specification.irn().isBlank()) return "IRN missing";
        if(specification.latency() < 0) return "Latency should be above or equal to 0";
        if(specification.storage() <= 0) return "Storage capacity probably shouldn't be 0 or less";
        return null;
    }
}
