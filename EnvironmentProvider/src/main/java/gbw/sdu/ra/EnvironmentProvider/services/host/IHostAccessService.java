package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;

public interface IHostAccessService {
    String OS = System.getProperty("os.name").toLowerCase();
    boolean IS_WINDOWS = OS.startsWith("windows");
    boolean IS_LINUX = OS.startsWith("linux");
    boolean IS_MACOS = OS.startsWith("macos");

    boolean isDockerRunning();
    ValErr<String,Exception> getIpv4();
    ValErr<String,Exception> getApplicationWD();
    ValErr<Integer,Exception> getAvailablePort();
    Exception verifyHostAndInit();
}
