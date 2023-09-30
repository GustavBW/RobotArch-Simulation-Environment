package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.configurations.MetadataTemplate;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MetadataAggregator {
    private final EnvironmentCache environment;
    private final RestTemplate template;

    @Autowired
    public MetadataAggregator(EnvironmentCache environment, RestTemplate template) {
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
