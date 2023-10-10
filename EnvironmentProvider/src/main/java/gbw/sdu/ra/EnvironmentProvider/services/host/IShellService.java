package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;

public interface IShellService {
    /**
     * @param cmd Does not support white-space names - because windows amirite
     * @return Either an error as soon as possible, or the exit code of the process
     * Duly note that exit code 0 means success although Boolean.parseBoolean((int) 0) == false.
     */
    ValErr<Integer,Exception> execSeqSync(String[] cmd);
    /**
     * @param cmd The command to be run as a string array
     * @param gobbler The handler for the process, the shell logger is applied by default.
     * @return Either an error as soon as possible, or the exit code of the process
     * Duly note that exit code 0 means success although Boolean.parseBoolean((int) 0) == false.
     */
    ValErr<Integer,Exception> execSeqSync(String[] cmd, ProcessOutputHandler gobbler);
}
