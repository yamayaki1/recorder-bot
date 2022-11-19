package me.yamayaki.musicbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class Config {
    private static final Properties internalValues = new Properties();
    private static final Properties userDefined = new Properties();

    static {
        try (InputStream inputStream = Config.class.getResourceAsStream("/recorderbot.properties")) {
            assert inputStream != null;
            internalValues.load(inputStream);
        } catch (IOException | NullPointerException ignored) {
        }
    }

    private final String[] availableSettings = new String[]{
            "discord.token",
            "spotify.id",
            "spotify.secret"
    };

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Config(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }

        final File settingsFile = new File(file, "settings.properties");

        this.load(settingsFile);
        this.write(settingsFile);
    }

    public static String getVersion() {
        return internalValues.getProperty("version", "unknown");
    }

    public static boolean isDevBuild() {
        return Boolean.parseBoolean((String) internalValues.getOrDefault("devbuild", "true"));
    }

    private void load(File settingsFile) {
        try (InputStream inputStream = new FileInputStream(settingsFile)) {
            userDefined.load(inputStream);
        } catch (Exception ignored) {
        }

        for (String availableSetting : availableSettings) {
            userDefined.putIfAbsent(availableSetting, "");
        }
    }

    private void write(File settingsFile) {
        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            userDefined.store(fileWriter, "");
        } catch (Exception ignored) {
        }
    }

    public String getSetting(String key) {
        if (Arrays.stream(availableSettings).noneMatch(key::equalsIgnoreCase)) {
            throw new IllegalArgumentException("unknown setting key " + key);
        }

        return userDefined.getProperty(key);
    }
}
