package me.jerriidesu.musicbot.utils;

import fr.free.nrw.jakaroma.Jakaroma;

import java.net.MalformedURLException;
import java.net.URL;

public class StringTools {
    private static final Jakaroma jakaroma = new Jakaroma();

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String romanize(String string) {
        return jakaroma.convert(string, false, false);
    }
}
