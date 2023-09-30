package gbw.sdu.ra.EnvironmentProvider.services.docker;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import gbw.sdu.ra.EnvironmentProvider.services.environment.EnvironmentEntry;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DockerServerDeployer {


    public ValErr<EnvironmentEntry,Exception> deployServerSync(File referenceFile, ServerSpecification specification){
        return ValErr.error(new Exception("Not Implimented"));
    }

    public void buildImage(){
        //docker build --build-arg SOURCE_DIR=<>

    }

}
