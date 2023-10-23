package gbw.sdu.ra.EnvironmentProvider.services.docker;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import gbw.sdu.ra.EnvironmentProvider.services.environment.EnvironmentRegistry;
import gbw.sdu.ra.EnvironmentProvider.services.environment.EnvironmentEntry;
import gbw.sdu.ra.EnvironmentProvider.services.environment.MetadataAggregator;
import gbw.sdu.ra.EnvironmentProvider.services.host.HostAccessService;
import gbw.sdu.ra.EnvironmentProvider.services.host.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DockerServerDeployer {

    private final EnvironmentRegistry environment;
    private final HostAccessService hostService;
    private final ShellService shell;
    @Autowired
    public DockerServerDeployer(ShellService shell, EnvironmentRegistry environment, HostAccessService hostService){
        this.environment = environment;
        this.hostService = hostService;
        this.shell = shell;
    }

    public ValErr<EnvironmentEntry,Exception> deployServerSync(File referenceFile, ServerSpecification specification){
        // <---------------- Building dockerfile ---------------->
        DockerfileBuilder fileBuilder = new DockerfileBuilder();
        Exception fromFileExc = fileBuilder.buildFromFile(referenceFile); //"exception not thrown!" - yes. Exactly.
        if(fromFileExc != null) return ValErr.error(fromFileExc);

        fileBuilder.fillSpecificationEnvVars(specification);
        long id = environment.getNextId();
        ValErr<Integer,Exception> portAttempt = hostService.getAvailablePort();
        if(portAttempt.hasError()) return ValErr.error(portAttempt.err());
        int hostPort = portAttempt.val();

        ValErr<String,Exception> getIpAttempt = hostService.getIpv4();
        if(getIpAttempt.hasError()) return ValErr.error(getIpAttempt.err());
        String ip = getIpAttempt.val();
        fileBuilder.fillHostSpecifcEnvVars(id, hostPort, ip);

        ValErr<String,Exception> saveToFileAttempt = fileBuilder.saveAndGetPath();
        if(saveToFileAttempt.hasError()) return ValErr.error(saveToFileAttempt.err());
        String dockerfilePath = saveToFileAttempt.val();
        String imageName = specification.asCompressedString() + ":" + fileBuilder.getApiVersion();

        // <---------------- Building Image ---------------->
        Exception buildError = buildImage(imageName,fileBuilder.getSourceRootDir(), dockerfilePath, specification);
        if(buildError != null) return ValErr.error(buildError);

        // <---------------- Starting Container ---------------->
        Exception containerStartError = deployContainer(imageName, fileBuilder, hostPort);
        if (containerStartError != null) return ValErr.error(containerStartError);

        String metadataUrl = ip + ":" + hostPort + "/api/" + fileBuilder.getApiVersion() + "/metadata";
        return ValErr.value(environment.addDeployed(id, metadataUrl, specification));
    }

    Exception deployContainer(String imageName, DockerfileBuilder fileBuilder, int hostPort) {
        String[] runCmd = new String[]{
                "docker", "run", imageName,
                "--name=RA_SIM_"+ fileBuilder.getFileName(),
                "--expose=" + hostPort + ":" + fileBuilder.getServerPort(),
        };
        ValErr<Integer,Exception> startContainer = shell.execSeqSync(runCmd);
        if(startContainer.hasError()) return startContainer.err();
        if(startContainer.val() != 0) return new Exception("Start container cmd ended with an unexpected exit code");
        return null;
    }

    Exception buildImage(String imageName, String dockerBuildContext, String dockerFileName, ServerSpecification specification){
        String[] buildCmd = new String[]{
                "docker","build",
                "--tag=\"" + imageName +"\"",
                "--file=\"" + dockerFileName + "\"",
                "--memory=" + specification.memory(),
                "--cpu-shares=" + specification.cpus(),
                dockerBuildContext
        };
        ValErr<Integer,Exception> buildImage = shell.execSeqSync(buildCmd);
        if(buildImage.hasError()) return buildImage.err();
        if(buildImage.val() != 0) return new Exception("Build cmd ended with an unexpected exit code");
        return null;
    }


}
