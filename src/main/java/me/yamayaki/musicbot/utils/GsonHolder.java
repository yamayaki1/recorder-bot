package me.yamayaki.musicbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonHolder {
    private static final Gson GSON;

    static {
        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    public static Gson getGson() {
        return GSON;
    }
}
