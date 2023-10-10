package gbw.sdu.ra.EnvironmentProvider.services.host;

import gbw.sdu.ra.EnvironmentProvider.ValErr;
import gbw.sdu.ra.EnvironmentProvider.services.docker.DockerfileBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.Inet4Address;
import java.net.ServerSocket;

@Service
public class HostAccessService implements IHostAccessService {

    private final ShellService shell;

    @Autowired
    public HostAccessService(ShellService shell){
        this.shell = shell;
    }

    @Override
    public Exception verifyHost(){
        if(!isDockerRunning()) return new Exception("Docker is not accessible!");
        ValErr<String,Exception> getIpv4 = getIpv4();
        if(getIpv4.hasError()) return getIpv4.err();
        ValErr<String,Exception> getWD = getApplicationWD();
        if(getWD.hasError()) return getWD.err();
        Exception fileBuilderInit = DockerfileBuilder.init();
        if(fileBuilderInit != null) return fileBuilderInit;
        return shell.init();
    }
    @Override
    public ValErr<Integer,Exception> getAvailablePort(){
        try (ServerSocket socket = new ServerSocket(0)){
            return ValErr.value(socket.getLocalPort());
        }catch(Exception something){
            return ValErr.error(something);
        }
    }

    private String cachedIpv4 = null;
    @Override
    public ValErr<String,Exception> getApplicationWD(){
        File here = new File(".");
        return ValErr.encapsulate(here::getCanonicalPath);
    }
    @Override
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

    @Override
    public boolean isDockerRunning(){
        ValErr<Integer,Exception> getExitCode = shell.execSeqSync(new String[]{"docker","info"});
        if(getExitCode.val() != null){
            return getExitCode.val() == 0;
        }
        return false;
    }



}
