package gbw.sdu.ra.EnvironmentProvider.services.functional;

@FunctionalInterface
public interface Verifier<T> {

    /**
     * @param obj to verify
     * @return null on valid object, else a String describing the issue
     */
    String verify(T obj);
}
