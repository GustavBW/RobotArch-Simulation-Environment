package gbw.sdu.ra.EnvironmentProvider.services.docker;

public enum RAEnvVars {
    //Injected:
    CONTAINER_EXTERNAL_PORT("RA_CONTAINER_EXTERNAL_PORT"),
    ENVIRONMENT_ID("RA_ENVIRONMENT_ID"),
    IRN("RA_IRN"),
    CONTAINER_HOST_IP("RA_CONTAINER_HOST_IP"),
    STATIC_LATENCY("RA_STATIC_LATENCY"),
    MEMORY("RA_MEMORY"),
    CPUS("RA_CPUS"),
    STORAGE("RA_STORAGE"),

    //Expected:
    API_VERSION("RA_API_VERSION"),
    INTERNAL_SERVER_PORT("RA_INTERNAL_SERVER_PORT"),
    SOURCE("RA_SOURCE");

    public final String value;
    RAEnvVars(String value){
        this.value = value;
    }
}
