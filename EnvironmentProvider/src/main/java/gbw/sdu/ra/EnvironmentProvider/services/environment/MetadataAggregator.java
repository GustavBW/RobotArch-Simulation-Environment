package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Stream;

@Service
public class MetadataAggregator {
    private final EnvironmentRegistry environment;
    private final RestTemplate template;

    @Autowired
    public MetadataAggregator(EnvironmentRegistry environment, RestTemplate template) {
        this.environment = environment;
        this.template = template;
    }

    public Stream<ValErr<ServerMetadata,Exception>> gatherAll() {
        return environment.entries().parallelStream().map(this::getFor);
    }

    public ValErr<ServerMetadata,Exception> getFor(EnvironmentEntry entry) {
        return getFor(entry.absoluteMetadataURL());
    }
    public ValErr<ServerMetadata,Exception> getFor(String url) {
        return ValErr.encapsulate(() -> template.getForObject(url,ServerMetadata.class));
    }
}
