package gbw.sdu.ra.EnvironmentProvider.services.docker;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import gbw.sdu.ra.EnvironmentProvider.services.Timestamped;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public interface IDockerfileBuilder {
    Exception buildFromFile(String referenceFileName);
    Exception buildFromFile(File referenceFile);
    void fillHostSpecifcEnvVars(long environmentId, int port, String hostIpv4);
    void fillSpecificationEnvVars(ServerSpecification specification);
    void addEnv(String key, String value);
    void addComment(String segmentKey, String comment);

    ValErr<String,Exception> saveAndGetPath();
    String getApiVersion();
    /**
     * When saved, the isolated filename is accessible.
     */
    String getFileName();
    int getServerPort();
}
