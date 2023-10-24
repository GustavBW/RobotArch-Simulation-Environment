package gbw.sdu.ra.EnvironmentProvider.services.environment;

import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;

public record EnvironmentEntry(long id, String absoluteMetadataURL, ServerSpecification specification) {
    @Override
    public String toString(){
        return "EnvironmentEntry{\"id\":" + id + ",\"absoluteMetadataURL\":" + absoluteMetadataURL + ",\"specification\":" + specification + "}";
    }
}
