package me.yamayaki.musicbot.tasks;

import me.yamayaki.musicbot.MusicBot;

public record BackgroundTasks(MusicBot instance) implements Runnable {
    @Override
    public void run() {
        try {
            this.updateAudioManager();
            MusicBot.DATABASE.flush();
        } catch (Exception e) {
            MusicBot.LOGGER.error(e);
        }
    }

    private void updateAudioManager() {
        this.instance.getAllAudioManagers().forEach((server, serverAudioManager) -> {
            if (serverAudioManager.isInactive()) {
                this.instance.removeAudioManager(server);
                MusicBot.LOGGER.info("removing server audio manager for {} as it's been inactive for a while.", server.getName());
                return;
            }

            serverAudioManager.getPlayerControl().updateMessage();
        });
    }
}
