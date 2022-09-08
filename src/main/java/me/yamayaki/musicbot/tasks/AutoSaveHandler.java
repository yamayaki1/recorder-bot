package me.yamayaki.musicbot.tasks;

import me.yamayaki.musicbot.MusicBot;

public class AutoSaveHandler implements Runnable {
    @Override
    public void run() {
        MusicBot.getCache().saveFile();
    }
}
