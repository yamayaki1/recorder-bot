package me.yamayaki.musicbot.tasks;

import me.yamayaki.musicbot.MusicBot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;

public record CmdLineHandler(MusicBot instance) implements Runnable {
    public static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void run() {
        String command;

        while (true) {
            try {
                command = br.readLine();
                if (command != null) {
                    this.runCommand(command);
                }

                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
    }

    public void runCommand(String command) {
        switch (command) {
            case "end", "stop" -> System.exit(15);
            case "status" -> {
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

                MusicBot.LOGGER.info("- GateWay-Ping: {}", this.instance.getAPI().getLatestGatewayLatency().getSeconds());
                MusicBot.LOGGER.info("- Running Threads:");
                threadSet.forEach(thread -> MusicBot.LOGGER.info(threadToString(thread)));
            }
            case "reset_commands" -> {
                MusicBot.LOGGER.info("deleting commands...");
                this.instance.getAPI().getGlobalSlashCommands().thenAccept(list -> list.forEach(cmd -> cmd.deleteGlobal().join()));

                MusicBot.LOGGER.info("deleted commands, stopping instance ...");
                this.instance.shutdown();
            }
        }
    }

    private String threadToString(Thread thread) {
        return String.format("-- %d %s: %s", thread.getId(), thread.getName(), thread.getState().name());
    }
}
