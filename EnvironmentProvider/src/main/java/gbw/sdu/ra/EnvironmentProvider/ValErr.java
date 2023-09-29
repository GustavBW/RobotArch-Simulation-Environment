package gbw.sdu.ra.EnvironmentProvider;

//Because GoLang did it right
public record ValErr<T,R>(T val, R err) {

    @FunctionalInterface
    public interface VoidTupleRetriever<M extends Throwable> {
        void run() throws M;
    }

    @FunctionalInterface
    public interface ZeroParameterTupleRetriever<K, M extends Throwable> {
        K run() throws M;
    }
    @FunctionalInterface
    public interface OneParameterTupleRetriever<K, N, M extends Throwable> {
        K run(N value) throws M;
    }
    @FunctionalInterface
    public interface TwoParameterTupleRetriever<K, N, L, M extends Throwable> {
        K run(N var1, L var2) throws M;
    }


    public static <T, R> ValErr<T, R> value(T value) {
        return new ValErr<>(value, null);
    }

    public static <T, R> ValErr<T, R> error(R error) {
        return new ValErr<>(null, error);
    }

    public static <T, R> ValErr<T, R> of(T value, R error) {
        return new ValErr<>(value, error);
    }

    @SuppressWarnings("unchecked")
    public static <M extends Throwable> M encapsulate(VoidTupleRetriever<M> retriever){
        try{
            retriever.run();
        }catch (Throwable e){
            return (M) e;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends Throwable> ValErr<T, R> encapsulate(ZeroParameterTupleRetriever<T, R> retriever) {
        T value = null;
        try {
            value = retriever.run();
        } catch (Throwable e) {
            return of(value,(R) e);
        }
        return of(value, null);
    }


    @SuppressWarnings("unchecked")
    public static <T, N, R extends Throwable> ValErr<T, R> encapsulate(OneParameterTupleRetriever<T, N, R> retriever, N value) {
        T result = null;
        try {
            result = retriever.run(value);
        } catch (Throwable e) {
            return of(result,(R) e);
        }
        return of(result, null);
    }
    @SuppressWarnings("unchecked")
    public static <T,N,L,R extends Throwable> ValErr<T,R> encapsulate(TwoParameterTupleRetriever<T,N,L,R> retriever, N var1, L var2){
        T result = null;
        try {
            result = retriever.run(var1,var2);
        } catch (Throwable e) {
            return of(result,(R) e); //Java cant infer that "R extends Throwable" means that e - instance of R - is subtype of Throwable...
        }
        return of(result, null);
    }



    public boolean hasError(){
        return this.err() != null;
    }


    @Override
    public String toString(){
        return "<[value=" + val + " , error=" + err + "]>";
    }
}
