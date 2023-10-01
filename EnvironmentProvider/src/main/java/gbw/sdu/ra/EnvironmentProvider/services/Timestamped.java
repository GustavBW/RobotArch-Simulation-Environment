package gbw.sdu.ra.EnvironmentProvider.services;

public record Timestamped<T>(T data, long timestamp) {

    public static <T> Timestamped<T> now(T data){
        return new Timestamped<>(data, System.currentTimeMillis());
    }

}
