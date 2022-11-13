package me.yamayaki.musicbot.audio.entities;

public class LoaderResponse {
    private final boolean success;
    private final int count;
    private final String message;
    private final String title;

    public LoaderResponse(boolean success, int count) {
        this.success = success;
        this.count = count;
        this.message = "";
        this.title = "";
    }

    public LoaderResponse(boolean success, int count, String message) {
        this.success = success;
        this.count = count;
        this.message = message;
        this.title = "";
    }

    public LoaderResponse(boolean success, int count, String message, String title) {
        this.success = success;
        this.count = count;
        this.message = message;
        this.title = title;
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

    public String getTitle() {
        return this.title;
    }
}
