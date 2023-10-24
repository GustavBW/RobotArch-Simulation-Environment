package gbw.sdu.ra.EnvironmentProvider;

import gbw.sdu.ra.EnvironmentProvider.services.docker.DockerfileBuilder;
import gbw.sdu.ra.EnvironmentProvider.services.environment.IEnvironmentRegistry;
import gbw.sdu.ra.EnvironmentProvider.services.host.HostAccessService;
import gbw.sdu.ra.EnvironmentProvider.services.host.IHostAccessService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EnvironmentProviderApplication {

	private static ApplicationContext context;

	public static void main(String[] args) throws Exception {
		context = SpringApplication.run(EnvironmentProviderApplication.class, args);
		IHostAccessService hostService = context.getBean(IHostAccessService.class);
		IEnvironmentRegistry registry = context.getBean(IEnvironmentRegistry.class);
		Exception hostFailure = hostService.verifyHost();
		if(hostFailure != null){
			throw hostFailure;
		}else{
			System.out.println("EnvironmentProvider host verified and good to Go.");
		}
		System.out.println("EnvironmentProvider started with images:" + DockerfileBuilder.getReferenceFiles());
		Runtime.getRuntime().addShutdownHook(new Thread(registry::clear));
	}

}
