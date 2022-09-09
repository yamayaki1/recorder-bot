package me.yamayaki.musicbot.audio.handler;

public class LoaderResponse {
    private final boolean success;

    public LoaderResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return this.success;
    }
}
