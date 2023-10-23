package gbw.sdu.ra.EnvironmentProvider.dtos;

public record ServerSpecification(String irn, int memory, int cpus, int latency, int storage) {

    public String asCompressedString(){
        return irn+"_mmb_"+memory+"_cpus_"+cpus+"_lms_"+latency+"_smb_"+storage;
    }
}
