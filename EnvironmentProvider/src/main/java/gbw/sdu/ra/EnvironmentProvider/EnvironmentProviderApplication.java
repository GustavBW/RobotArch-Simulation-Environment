package gbw.sdu.ra.EnvironmentProvider;

import gbw.sdu.ra.EnvironmentProvider.services.docker.DockerfileBuilder;
import gbw.sdu.ra.EnvironmentProvider.services.host.HostAccessService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EnvironmentProviderApplication {

	private static ApplicationContext context;

	public static void main(String[] args) throws Exception {
		context = SpringApplication.run(EnvironmentProviderApplication.class, args);
		HostAccessService hostService = context.getBean(HostAccessService.class);
		Exception hostFailure = hostService.verifyHost();
		if(hostFailure != null){
			throw hostFailure;
		}else{
			System.out.println("EnvironmentProvider host verified and good to Go.");
		}
		System.out.println("EnvironmentProvider started with images:" + DockerfileBuilder.getReferenceFiles());
	}

}
