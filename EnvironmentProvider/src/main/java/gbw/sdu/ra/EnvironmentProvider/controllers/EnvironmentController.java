package gbw.sdu.ra.EnvironmentProvider.controllers;

import gbw.sdu.ra.EnvironmentProvider.REBuilder;
import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import gbw.sdu.ra.EnvironmentProvider.services.environment.EnvironmentCache;
import gbw.sdu.ra.EnvironmentProvider.services.environment.ParallelMetadataGatherer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = "application/json")
public class EnvironmentController {

    private final EnvironmentCache environment;
    private final ParallelMetadataGatherer gatherer;

    @Autowired
    public EnvironmentController(EnvironmentCache environment, ParallelMetadataGatherer gatherer){
        this.environment = environment;
        this.gatherer = gatherer;
    }

    @PostMapping("/api/v1/clear")
    public @ResponseBody ResponseEntity<?> clearEnvironment(){
        environment.clear();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/metadata")
    public @ResponseBody ResponseEntity<Map<String,List<ServerMetadata>>> getEnvironmentMetadata(){
        if(environment.isEmpty()){
            return new REBuilder<Map<String,List<ServerMetadata>>>()
                    .addHeader("DDH","No environment established")
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        List<ServerMetadata> filtered = gatherer.gatherCurrent()
                .stream().filter(ValErr::hasError).map(ValErr::val).toList();
        Map<String,List<ServerMetadata>> responseMap = new HashMap<>();
        filtered.forEach(e -> responseMap.computeIfAbsent(e.specification().irn(), k -> new ArrayList<>()).add(e));

        if(filtered.size() < environment.getCloudSize()){
            return new REBuilder<>(responseMap)
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .build();
        }

        return new REBuilder<>(responseMap)
                .status(HttpStatus.OK)
                .build();
    }

}
