package gbw.sdu.ra.EnvironmentProvider.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MetadataTemplate{

    @Bean
    public RestTemplate getTemplate(){
        return new RestTemplate();
    }
}
