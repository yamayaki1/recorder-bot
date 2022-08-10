package me.jerriidesu.musicbot.tasks;

import me.jerriidesu.musicbot.MusicBot;

public class AutoSaveHandler implements Runnable {
    @Override
    public void run() {
        MusicBot.getCache().saveFile();
    }
}
