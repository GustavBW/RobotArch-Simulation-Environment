package gbw.sdu.ra.EnvironmentProvider.dtos;

public record ServerSpecification(String irn, int memory, float cpus, int latency, int storage) {

    public String asCompressedString(){
        return "IRN="+irn+"&Mmb="+memory+"&CPUS="+cpus+"Lms="+latency+"Smb="+storage;
    }
}
