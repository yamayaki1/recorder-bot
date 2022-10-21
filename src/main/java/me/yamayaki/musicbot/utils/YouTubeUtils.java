package me.yamayaki.musicbot.utils;

public class YouTubeUtils {
    public static String getThumbnail(String youTubeId) {
        return "https://img.youtube.com/vi/" + youTubeId + "/hqdefault.jpg";
    }
}
