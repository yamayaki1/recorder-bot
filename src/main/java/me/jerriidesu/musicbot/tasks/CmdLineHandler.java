package me.jerriidesu.musicbot.tasks;

import me.jerriidesu.musicbot.MusicBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public record CmdLineHandler(MusicBot instance) implements Runnable {

    public static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void run() {
        try {
            String command = br.readLine();
            if (command != null) {
                this.runCommand(command);
            }
        } catch (IOException ignored) {
        }
    }

    public void runCommand(String command) {
        switch (command) {
            case "end", "stop" -> {
                this.instance.getAPI().disconnect();
                MusicBot.getExecutors().shutdown();

                System.exit(0);
            }
            case "status" -> {
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

                MusicBot.getLogger().info("- GateWay-Ping: {}", this.instance.getAPI().getLatestGatewayLatency().getSeconds());
                MusicBot.getLogger().info("- Running Threads:");
                threadSet.forEach(thread -> MusicBot.getLogger().info(threadToString(thread)));
            }
            case "reload" -> {
                MusicBot.getLogger().info("reloading config...");
                MusicBot.getConfig().reload();
                this.instance.updatePresence();
            }
        }
    }

    private String threadToString(Thread thread) {
        return String.format("-- %d %s: %s", thread.getId(), thread.getName(), thread.getState().name());
    }
}
