package gbw.sdu.ra.EnvironmentProvider;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.util.Map;

//The simple version
public class REBuilder<T> {

    public static final String DDH = "SDU-RA-Debug-Header";
    private T body;
    private final MultiValueMap<String,String> headers = new HttpHeaders();
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public static <T> REBuilder<T> create(){
        return new REBuilder<T>();
    }

    public REBuilder(T body){
        this.body = body;
    }
    public REBuilder(){
    }

    public REBuilder<T> body(T body){
        this.body = body;
        return this;
    }
    public REBuilder<T> addHeader(String key, String value){
        headers.add(key,value);
        return this;
    }
    public REBuilder<T> status(HttpStatus status){
        this.status = status;
        return this;
    }

    public ResponseEntity<T> build(){
        return new ResponseEntity<>(body, headers, status);
    }

}
