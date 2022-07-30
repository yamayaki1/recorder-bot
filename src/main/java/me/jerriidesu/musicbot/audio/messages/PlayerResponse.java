package me.jerriidesu.musicbot.audio.messages;

public class PlayerResponse {
    private final boolean success;
    private final String message;

    public PlayerResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
