package me.jerriidesu.musicbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BotConfig {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Path filePath;

    private JsonConfig config;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public BotConfig(File file) {
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
        MusicBot.getLogger().info("reloading config...");
        this.config = null;

        try {
            this.config = this.gson.fromJson(new String(Files.readAllBytes(this.filePath)), JsonConfig.class);
        } catch (IOException e) {
            System.out.println("error reading settings file, generating new one");
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
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal"})
    public static class JsonConfig {
        private BotOptions bot = new BotOptions();
        private Reactions reactions = new Reactions();

        public BotOptions getBot() {
            return bot;
        }

        public Reactions getReactions() {
            return this.reactions;
        }

        @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal", "FieldCanBeLocal"})
        public static class BotOptions {
            private String status = "ONLINE";
            private String token = "";
            private String prefix = ":";

            public String getStatus() {
                return status;
            }

            public String getToken() {
                return token;
            }

            public char getPrefix() {
                return this.prefix.charAt(0);
            }
        }

        @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal", "FieldCanBeLocal"})
        public static class Reactions {
            private String success = ":thumbsup:";
            private String failure = ":x:";
            private String refuse = ":no_good:";

            public String getSuccess() {
                return success;
            }

            public String getFailure() {
                return failure;
            }

            public String getRefuse() {
                return refuse;
            }
        }
    }
}
