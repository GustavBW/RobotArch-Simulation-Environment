package gbw.sdu.ra.EnvironmentProvider.controllers;

import gbw.sdu.ra.EnvironmentProvider.dtos.ServerMetadata;
import gbw.sdu.ra.EnvironmentProvider.services.functional.Verifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/test")
public class TestController {

    private final Verifier<ServerMetadata> metadataVerifier;

    @Autowired
    public TestController(Verifier<ServerMetadata> metadataVerifier){
        this.metadataVerifier = metadataVerifier;
    }

    @PostMapping("/metadata-struct")
    public @ResponseBody ResponseEntity<String> testMetadataStruct(@RequestBody ServerMetadata metadata){
        String error = metadataVerifier.verify(metadata);
        if(error != null){
            return new ResponseEntity<>(
                    error,
                    HttpStatus.BAD_REQUEST
            );
        }

        return new ResponseEntity<>(
                metadataVerifier.verify(metadata),
                HttpStatus.OK
        );
    }

}
