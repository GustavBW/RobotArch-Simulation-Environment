package gbw.sdu.ra.EnvironmentProvider.dtos;

import java.util.Map;

public record ServerMetadata(long id, Map<String,ServerAction> actions, ServerSpecification specification, ServerUtilization utilization) {
}
