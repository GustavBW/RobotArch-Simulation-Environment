package gbw.sdu.ra.EnvironmentProvider;

import gbw.sdu.ra.EnvironmentProvider.services.host.HostInformationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EnvironmentProviderApplication {

	private static ApplicationContext context;

	public static void main(String[] args) throws Exception {
		//Rescan image directory

		System.out.println("EnvironmentProvider started with images: <arrToString>");

		context = SpringApplication.run(EnvironmentProviderApplication.class, args);
		HostInformationService hostService = context.getBean(HostInformationService.class);
		Exception hostFailure = hostService.verifyHost();
		if(hostFailure != null){
			throw hostFailure;
		}
	}

}
