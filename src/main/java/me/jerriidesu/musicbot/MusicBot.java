package me.jerriidesu.musicbot;

import me.jerriidesu.musicbot.audio.PlaylistManager;
import me.jerriidesu.musicbot.commands.CommandListener;
import me.jerriidesu.musicbot.tasks.CmdLineHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.user.UserStatus;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicBot {
    private static final Logger logger = LogManager.getLogger(MusicBot.class);
    private static final BotConfig botConfig = new BotConfig(new File(".", "config/"));
    private static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);

    private static PlaylistManager playlistManager = null;

    private DiscordApi discordApi;
    private Boolean isReady = false;

    public static void main(String[] args) {
        MusicBot bot = new MusicBot();
        bot.launch();
    }

    public static ScheduledExecutorService getExecutors() {
        return scheduledThreadPool;
    }

    public static BotConfig getConfig() {
        return botConfig;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    protected void launch() {
        //report version
        logger.info("starting music-bot ({}) ...", botConfig.getBotVersion());

        //init jda-builder
        logger.info("initializing discord-api ...");

        discordApi = new DiscordApiBuilder()
                .setToken(botConfig.get().getBot().getToken())
                .setIntents(Intent.GUILD_MESSAGE_REACTIONS, Intent.GUILD_MESSAGES, Intent.GUILD_VOICE_STATES)
                .addListener(new CommandListener())
                .login().join();

        logger.info("discord login successful, continuing ... ");

        playlistManager = new PlaylistManager(this);

        this.updatePresence();
        this.runTasks();

        this.isReady = true;
    }

    protected void runTasks() {
        logger.info("starting tasks and commandline listener ...");

        scheduledThreadPool.scheduleAtFixedRate(new CmdLineHandler(this), 0, 1, TimeUnit.SECONDS);
    }

    public void updatePresence() {
        discordApi.updateStatus(UserStatus.fromString(botConfig.get().getBot().getStatus()));
    }

    public Boolean isReady() {
        return this.isReady;
    }

    public DiscordApi getAPI() {
        return this.discordApi;
    }
}
