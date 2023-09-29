package gbw.sdu.ra.EnvironmentProvider.dtos;

public record ServerMetadata(long id, ServerAction[] actions, ServerSpecification specification, ServerUtilization utilization) {
}
