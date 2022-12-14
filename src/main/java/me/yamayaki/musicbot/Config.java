package me.yamayaki.musicbot;

import org.javacord.api.Javacord;
import org.rocksdb.RocksDB;

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

    private static boolean readOnly = false;

    static {
        try (InputStream inputStream = Config.class.getResourceAsStream("/recorderbot.properties")) {
            assert inputStream != null;
            internalValues.load(inputStream);
        } catch (IOException | NullPointerException ignored) {
        }
    }

    private final String[] availableSettings;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Config(File file, String[] availableSettings) {
        if (!file.exists()) {
            file.mkdirs();
        }

        final File settingsFile = new File(file, "settings.properties");

        this.availableSettings = availableSettings;

        this.load(settingsFile);
        this.write(settingsFile);
    }

    public static String getVersion() {
        return internalValues.getProperty("bot.version", "unknown");
    }

    public static boolean isDevBuild() {
        return !Boolean.parseBoolean(internalValues.getProperty("git.clean", "false"));
    }

    public static String getBranch() {
        return internalValues.getProperty("git.branch", "unknown");
    }

    public static String getDiscordVersion() {
        return "JavaCord-v" + Javacord.VERSION + "+rev." + Javacord.COMMIT_ID;
    }

    public static String getOsInfo() {
        return String.format("%s (%s) %s", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
    }

    public static String getRamInfo() {
        return ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "/" + (Runtime.getRuntime().totalMemory() / 1048576) + "MB";
    }

    public static String getDatabaseVersion() {
        return "RocksDB-v" + RocksDB.rocksdbVersion().toString();
    }

    private void load(File settingsFile) {
        try (InputStream inputStream = new FileInputStream(settingsFile)) {
            userDefined.load(inputStream);
        } catch (Exception error) {
            MusicBot.LOGGER.error("error while loading settings, falling back to read-only mode.", error);
            readOnly = true;
        }

        for (String availableSetting : availableSettings) {
            userDefined.putIfAbsent(availableSetting, "");
        }
    }

    private void write(File settingsFile) {
        if (readOnly) {
            MusicBot.LOGGER.error("not writing changes to settings file, as we're currently in read-only mode.");
            return;
        }

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
