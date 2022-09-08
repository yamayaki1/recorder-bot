package me.yamayaki.musicbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Path filePath;

    private JsonConfig config;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Config(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }

        this.filePath = new File(file, "settings.json").toPath();
        this.reload();
    }

    public String getBotVersion() {
        try (InputStream inputStream = this.getClass().getResourceAsStream("/version.txt")) {
            assert inputStream != null;
            return new String(inputStream.readAllBytes());
        } catch (IOException | NullPointerException e) {
            return "unknown";
        }
    }

    public JsonConfig get() {
        return this.config;
    }

    public void reload() {
        MusicBot.LOGGER.info("reloading config...");
        this.config = null;

        try {
            this.config = this.gson.fromJson(new String(Files.readAllBytes(this.filePath)), JsonConfig.class);
        } catch (IOException e) {
            MusicBot.LOGGER.error("error reading settings file, generating new one");
            this.config = new JsonConfig();
        }

        this.save();
    }

    public void save() {
        try {
            FileWriter writer = new FileWriter(this.filePath.toFile());
            writer.write(this.gson.toJson(this.config));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            MusicBot.LOGGER.error("error writing config file", e);
        }
    }

    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal"})
    public static class JsonConfig {
        private BotOptions bot = new BotOptions();
        private SpotifyConfig spotify = new SpotifyConfig();

        public BotOptions getBot() {
            return bot;
        }

        public SpotifyConfig getSpotify() {
            return this.spotify;
        }

        @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal", "FieldCanBeLocal"})
        public static class BotOptions {
            private String status = "ONLINE";
            private String token = "";

            public String getStatus() {
                return status;
            }

            public String getToken() {
                return token;
            }
        }

        @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal", "FieldCanBeLocal"})
        public static class SpotifyConfig {
            private String client_id = "";
            private String client_secret = "";

            public String getClientId() {
                return this.client_id;
            }

            public String getClientSecret() {
                return this.client_secret;
            }
        }
    }
}