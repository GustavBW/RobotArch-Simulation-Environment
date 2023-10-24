package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;

import java.util.List;

public interface IEnvironmentRegistry {

    ValErr<ServerMetadata,Exception> register(ServerMetadata metadata, String absoluteMetadataUrl);
    int getCloudSize();
    Exception clear();
    boolean isEmpty();
    long getNextId();
    EnvironmentEntry addDeployed(long id, String metadataUrl, ServerSpecification specification);
    List<EnvironmentEntry> entries();
}
