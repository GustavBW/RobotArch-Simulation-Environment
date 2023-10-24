package gbw.sdu.ra.EnvironmentProvider.dtos;


/**
 *
 * @param method
 * @param ip
 * @param port
 * @param uri including api versioning, i.e. "/api/v1/metadata"
 */
public record ServerAction(String method, String ip, int port, String uri) {

    public enum Known {
        START_PROCESS("startProcess"),
        GET_METADATA("getMetadata"),
        SHUTDOWN("shutdown");
        public final String value;
        Known(String value){
            this.value = value;
        }
    }

}
