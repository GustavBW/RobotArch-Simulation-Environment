package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.Inet4Address;
import java.net.ServerSocket;

@Service
public class HostInformationService {

    public static String OS = System.getProperty("os.name").toLowerCase();
    public static boolean IS_WINDOWS = OS.startsWith("windows");
    public static boolean IS_UNIX = OS.startsWith("unix");

    private final ShellService shell;

    @Autowired
    public HostInformationService(ShellService shell){
        this.shell = shell;
    }

    public Exception verifyHost(){
        if(!isDockerRunning()) return new Exception("Docker is not accessible!");
        ValErr<String,Exception> getIpv4 = getIpv4();
        if(getIpv4.hasError()) return getIpv4.err();
        ValErr<String,Exception> getWD = getApplicationWD();
        if(getWD.hasError()) return getWD.err();
        return null;
    }

    public ValErr<Integer,Exception> getAvailablePort(){
        try (ServerSocket socket = new ServerSocket(0)){
            return ValErr.value(socket.getLocalPort());
        }catch(Exception something){
            return ValErr.error(something);
        }
    }

    private String cachedIpv4 = null;

    public ValErr<String,Exception> getApplicationWD(){
        File here = new File(".");
        return ValErr.encapsulate(here::getCanonicalPath);
    }

    public ValErr<String,Exception> getIpv4(){
        if(cachedIpv4 == null){
            ValErr<String,Exception> attempt = ValErr.encapsulate(() -> Inet4Address.getLocalHost().getHostAddress());
            if(attempt.hasError()){
                return attempt;
            }
            this.cachedIpv4 = attempt.val();
        }
        return ValErr.value(cachedIpv4);
    }

    private final String[] silentDockerInfoCMD = new String[]{"docker","info",">","/dev/null","2>&1"};
    private final String[] getLatestProcessExitCode = new String[]{"$?"};

    public boolean isDockerRunning(){
        ValErr<Integer,Exception> tryExec = shell.execSeqSync(silentDockerInfoCMD);
        ValErr<Integer,Exception> getExitCode = shell.execSeqSync(getLatestProcessExitCode);
        if(getExitCode.val() != null){
            return getExitCode.val() == 0;
        }
        return false;
    }



}
