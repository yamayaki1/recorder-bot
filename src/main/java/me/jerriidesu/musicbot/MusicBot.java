package me.jerriidesu.musicbot;

import me.jerriidesu.musicbot.audio.ServerManager;
import me.jerriidesu.musicbot.audio.source.spotify.SpotifyAccess;
import me.jerriidesu.musicbot.commands.CommandListener;
import me.jerriidesu.musicbot.tasks.CmdLineHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.user.UserStatus;
import oshi.SystemInfo;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicBot {
    private static final Logger logger = LogManager.getLogger(MusicBot.class);
    private static final SystemInfo systemInfo = new SystemInfo();
    private static final BotConfig botConfig = new BotConfig(new File(".", "config/"));
    private static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);

    private static final SpotifyAccess spotifyAccess = new SpotifyAccess();

    private static ServerManager serverManager = null;

    private DiscordApi discordApi;
    private CommandListener commandListener = null;

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

    public static ServerManager getAudioManager() {
        return serverManager;
    }

    public static SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public static SpotifyAccess getSpotifyAccess() {
        return spotifyAccess;
    }

    protected void launch() {
        //report version
        logger.info("starting music-bot ({}) ...", botConfig.getBotVersion());

        //init jda-builder
        logger.info("initializing discord-api ...");

        discordApi = new DiscordApiBuilder()
                .setToken(botConfig.get().getBot().getToken())
                .setIntents(Intent.GUILD_MESSAGE_REACTIONS, Intent.GUILD_MESSAGES, Intent.GUILD_VOICE_STATES)
                .login().join();

        logger.info("discord login successful, continuing ... ");

        serverManager = new ServerManager(this);

        this.registerCommands();
        this.updatePresence();
        this.runTasks();
    }

    protected void runTasks() {
        logger.info("starting tasks and commandline listener ...");

        scheduledThreadPool.scheduleAtFixedRate(new CmdLineHandler(this), 0, 1, TimeUnit.SECONDS);
    }

    public void registerCommands() {
        //remove old listener if one is existing
        if (this.commandListener != null) {
            this.discordApi.removeListener(this.commandListener);
            this.commandListener = null;
        }

        this.commandListener = new CommandListener();
        this.discordApi.addListener(this.commandListener);
    }

    public void updatePresence() {
        discordApi.updateStatus(UserStatus.fromString(botConfig.get().getBot().getStatus()));
    }

    public DiscordApi getAPI() {
        return this.discordApi;
    }
}
