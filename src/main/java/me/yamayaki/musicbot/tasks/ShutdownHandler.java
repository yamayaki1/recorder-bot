package me.yamayaki.musicbot.tasks;

import me.yamayaki.musicbot.MusicBot;

public class ShutdownHandler extends Thread {
    private final MusicBot instance;

    public ShutdownHandler(MusicBot musicBot) {
        this.instance = musicBot;
    }

    @Override
    public void run() {
        this.instance.shutdown();
    }
}