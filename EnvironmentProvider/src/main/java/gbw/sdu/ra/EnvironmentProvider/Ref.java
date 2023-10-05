package gbw.sdu.ra.EnvironmentProvider;

public class Ref<T> {
    private T obj;
    public void set(T obj){
        this.obj = obj;
    }
    public T get(){
        return obj;
    }
}
