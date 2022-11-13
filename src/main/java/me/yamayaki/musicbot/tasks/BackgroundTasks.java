package me.yamayaki.musicbot.tasks;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.utils.Threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public record BackgroundTasks(MusicBot instance) implements Runnable {
    public static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void run() {
        try {
            switch (br.readLine()) {
                case "end", "stop" -> System.exit(15);
                case "status" -> {
                    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

                    MusicBot.LOGGER.info("- GateWay-Ping: {}", this.instance.getAPI().getLatestGatewayLatency().getSeconds());
                    MusicBot.LOGGER.info("- Running Threads:");
                    threadSet.forEach(thread -> MusicBot.LOGGER.info(Threads.threadToString(thread)));
                }
                case "reset_commands" -> {
                    MusicBot.LOGGER.info("deleting commands...");
                    this.instance.getAPI().getGlobalSlashCommands().thenAccept(list -> list.forEach(cmd -> cmd.deleteGlobal().join()));

                    MusicBot.LOGGER.info("deleted commands, stopping instance ...");
                    this.instance.shutdown();
                }
            }
        } catch (IOException e) {
            MusicBot.LOGGER.error(e);
        }
    }
}
