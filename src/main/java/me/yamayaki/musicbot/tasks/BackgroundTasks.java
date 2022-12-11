package me.yamayaki.musicbot.tasks;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.utils.Threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record BackgroundTasks(MusicBot instance) implements Runnable {
    public static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void run() {
        try {
            CompletableFuture.supplyAsync(()-> {
                try {
                    this.readCommandLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }).orTimeout(1L, TimeUnit.SECONDS);

            this.cleanupAudioManager();
        } catch (Exception e) {
            MusicBot.LOGGER.error(e);
        }
    }

    private void cleanupAudioManager() {
        this.instance.getAllAudioManagers().forEach((server, serverAudioManager) -> {
            if (!serverAudioManager.isInactive()) {
                return;
            }

            this.instance.removeAudioManager(server);
            MusicBot.LOGGER.info("removing server audio manager for {} as it's been inactive for a while.", server.getName());
        });
    }

    private void readCommandLine() throws IOException {
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
    }
}
