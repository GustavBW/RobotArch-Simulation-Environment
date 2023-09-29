package gbw.sdu.ra.EnvironmentProvider.dtos;

import org.springframework.http.HttpMethod;

public record ServerAction(HttpMethod method, String ip, short port, String url) {
}
