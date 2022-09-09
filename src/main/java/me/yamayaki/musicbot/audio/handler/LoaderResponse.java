package me.yamayaki.musicbot.audio.handler;

public class LoaderResponse {
    private final boolean success;
    private final int count;
    private final String message;

    public LoaderResponse(boolean success, int count) {
        this.success = success;
        this.count = count;
        this.message = "";
    }

    public LoaderResponse(boolean success, int count, String message) {
        this.success = success;
        this.count = count;
        this.message = message;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public int getCount() {
        return this.count;
    }

    public String getMessage() {
        return this.message;
    }
}
