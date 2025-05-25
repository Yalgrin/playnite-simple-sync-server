package pl.yalgrin.playnite.simplesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
public class PlayniteSimpleSyncServerApplication {

    public static void main(String[] args) {
        ReactorDebugAgent.init();
        SpringApplication.run(PlayniteSimpleSyncServerApplication.class, args);
    }

}
