package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;

public record EnvironmentEntry(long id, String absoluteMetadataURL, ServerSpecification specification) {
}
