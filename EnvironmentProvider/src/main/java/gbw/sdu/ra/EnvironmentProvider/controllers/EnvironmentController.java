package gbw.sdu.ra.EnvironmentProvider.controllers;

import gbw.sdu.ra.EnvironmentProvider.REBuilder;
import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerSpecification;
import gbw.sdu.ra.EnvironmentProvider.services.docker.DockerServerDeployer;
import gbw.sdu.ra.EnvironmentProvider.services.docker.DockerfileBuilder;
import gbw.sdu.ra.EnvironmentProvider.services.environment.EnvironmentRegistry;
import gbw.sdu.ra.EnvironmentProvider.services.environment.EnvironmentEntry;
import gbw.sdu.ra.EnvironmentProvider.services.environment.MetadataAggregator;
import gbw.sdu.ra.EnvironmentProvider.services.functional.Verifier;
import gbw.sdu.ra.EnvironmentProvider.services.host.HostAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping(path = "api/v1/",produces = "application/json")
public class EnvironmentController {

    private final EnvironmentRegistry environment;
    private final MetadataAggregator aggregator;
    private final HostAccessService hostService;
    private final Verifier<ServerSpecification> specVerifier;
    private final Verifier<ServerMetadata> metadataVerifier;
    private final DockerServerDeployer deployer;

    @Autowired
    public EnvironmentController(Verifier<ServerMetadata> metadataVerifier, DockerServerDeployer deployer, Verifier<ServerSpecification> specVerifier, EnvironmentRegistry environment, MetadataAggregator gatherer, HostAccessService hostService){
        this.environment = environment;
        this.aggregator = gatherer;
        this.hostService = hostService;
        this.specVerifier = specVerifier;
        this.deployer = deployer;
        this.metadataVerifier = metadataVerifier;
    }

    @PostMapping("/clear")
    public @ResponseBody ResponseEntity<?> clearEnvironment(){
        environment.clear();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metadata")
    public @ResponseBody ResponseEntity<Map<String,List<ServerMetadata>>> getEnvironmentMetadata(){
        if(environment.isEmpty()){
            return new REBuilder<Map<String,List<ServerMetadata>>>()
                    .addHeader(REBuilder.DDH,"No environment established")
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        List<ServerMetadata> filtered = aggregator.gatherAll()
                .filter(attempt -> !attempt.hasError())
                .map(ValErr::val)
                .toList();
        Map<String,List<ServerMetadata>> responseMap = new HashMap<>();
        filtered.forEach(e -> responseMap.computeIfAbsent(
                    e.specification().irn(), k -> new ArrayList<>()
                ).add(e)
        );

        if(filtered.size() < environment.getCloudSize()){
            return new REBuilder<>(responseMap)
                    .addHeader(REBuilder.DDH, "Some servers might not have responded in time. Anyhow, there's less responses than there should be.")
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .build();
        }

        return new REBuilder<>(responseMap)
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/server")
    public @ResponseBody ResponseEntity<ServerMetadata> addServer(@RequestBody ServerSpecification specification){
        //Verifying upfront dependencies to return early if there's a fundamental problem
        if(!hostService.isDockerRunning()){
            return new REBuilder<ServerMetadata>()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .addHeader(REBuilder.DDH, "Docker is not running on host.")
                    .build();
        }
        String specError = specVerifier.verify(specification);
        if(specError != null){
            return new REBuilder<ServerMetadata>()
                    .status(HttpStatus.NOT_ACCEPTABLE)
                    .addHeader(REBuilder.DDH, specError)
                    .build();
        }
        File referenceFile = DockerfileBuilder.getReferenceFiles().stream()
                .filter(file -> file.getName().startsWith(specification.irn()))
                .findFirst()
                .orElse(null);
        if(referenceFile == null){
            return new REBuilder<ServerMetadata>()
                    .status(HttpStatus.NOT_FOUND)
                    .addHeader(REBuilder.DDH, "No such reference file name.")
                    .build();
        }
        //actually deploying the thing: (might take a while)
        ValErr<EnvironmentEntry,Exception> deploymentAttempt = deployer.deployServerSync(referenceFile,specification);
        if(deploymentAttempt.hasError()){
            return new REBuilder<ServerMetadata>()
                    .addHeader(REBuilder.DDH, deploymentAttempt.err().getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
        //and finally:
        ValErr<ServerMetadata,Exception> gettingMetadata = aggregator.getFor(deploymentAttempt.val());
        if(gettingMetadata.hasError()){
            return new REBuilder<>(gettingMetadata.val()) //val might be null
                    .status(HttpStatus.FAILED_DEPENDENCY)
                    .addHeader(REBuilder.DDH, gettingMetadata.err().getMessage())
                    .build();
        }
        return new REBuilder<>(gettingMetadata.val())
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/reference-files")
    public @ResponseBody ResponseEntity<List<String>> getReferenceFiles(){
        return new REBuilder<>(DockerfileBuilder.getReferenceFiles().stream().map(File::getName).toList())
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/register")
    public @ResponseBody ResponseEntity<ServerMetadata> registerRemoteServer(@RequestParam(required = false) String metadataUrl){
        //I've actively disabled Spring own error handling by allowing a null url, but then handling it myself
        //This is done because Spring's error handling is a whole rat-pack of exceptions, and we don't do that here.
        if(metadataUrl == null){
            return new REBuilder<ServerMetadata>()
                    .status(HttpStatus.BAD_REQUEST)
                    .addHeader(REBuilder.DDH,"Missing query parameter: \"metadataUrl\"")
                    .build();
        }
        ValErr<ServerMetadata,Exception> reachAttempt = aggregator.getFor(metadataUrl);
        if(reachAttempt.hasError()){
            return new REBuilder<ServerMetadata>()
                    .addHeader(REBuilder.DDH, reachAttempt.err().getMessage())
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        //verify the metadata returned:
        String metadataError = metadataVerifier.verify(reachAttempt.val());
        if(metadataError != null){
            return new REBuilder<>(reachAttempt.val())
                    .status(HttpStatus.NOT_ACCEPTABLE)
                    .addHeader(REBuilder.DDH,metadataError)
                    .build();
        }
        //now register
        ValErr<ServerMetadata,Exception> registerAttempt = environment.register(reachAttempt.val());
        if(registerAttempt.hasError()){ //something went wrong... somehow
            return new REBuilder<>(reachAttempt.val())
                    .addHeader("DDH", registerAttempt.err().getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
        return new REBuilder<>(registerAttempt.val())
                .status(HttpStatus.OK)
                .build();
    }

}
