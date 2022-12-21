package me.yamayaki.musicbot.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class CommonUtils {
    private static final Gson GSON;

    static {
        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String getThumbnail(String youTubeId) {
        return "https://img.youtube.com/vi/" + youTubeId + "/hqdefault.jpg";
    }

    public static Gson gson() {
        return GSON;
    }
}
