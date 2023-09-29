package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.configurations.MetadataTemplate;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class ParallelMetadataGatherer {

    private final EnvironmentCache environment;
    private final MetadataTemplate template;
    @Autowired
    public ParallelMetadataGatherer(EnvironmentCache environment, MetadataTemplate template){
        this.environment = environment;
        this.template = template;
    }

    public List<ValErr<ServerMetadata,RuntimeException>> gatherCurrent(){
        return environment.entries()
                .parallelStream()
                .map(
                        entry -> ValErr.encapsulate(() -> template.getForObject(entry.absoluteMetadataURL(), ServerMetadata.class))
                )
                .toList();
    }



}
