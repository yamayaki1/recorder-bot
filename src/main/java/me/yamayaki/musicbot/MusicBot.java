package me.yamayaki.musicbot;

import me.yamayaki.musicbot.audio.ServerManager;
import me.yamayaki.musicbot.audio.source.spotify.SpotifyAccess;
import me.yamayaki.musicbot.database.RocksManager;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;
import me.yamayaki.musicbot.database.specs.impl.CacheSpecs;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.interactions.InteractionListener;
import me.yamayaki.musicbot.tasks.CmdLineHandler;
import me.yamayaki.musicbot.tasks.ShutdownHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.user.UserStatus;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicBot {
    public static final boolean DEBUG = false;

    public static final Logger LOGGER = LogManager.getLogger(MusicBot.class);
    public static final Config CONFIG = new Config(new File(".", "config/"));

    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static final RocksManager DATABASE = new RocksManager(new File(".", "database/"), new DatabaseSpec[]{
            CacheSpecs.SPOTIFY_CACHE,
            CacheSpecs.YOUTUBE_CACHE,
            CacheSpecs.PLAYLIST_CACHE,

            ChannelSpecs.CHANNEL_SETTINGS
    });

    private static final SpotifyAccess spotifyAccess = new SpotifyAccess();

    private static ServerManager serverManager = null;

    private final ScheduledExecutorService executorPool = Executors.newScheduledThreadPool(2);
    private DiscordApi discordApi;

    public static void main(String[] args) {
        MusicBot bot = new MusicBot();
        bot.launch();
    }

    public static ServerManager getAudioManager() {
        return serverManager;
    }

    public static SpotifyAccess getSpotifyAccess() {
        return spotifyAccess;
    }

    protected void launch() {
        //report version
        LOGGER.info("starting music-bot ({}) ...", CONFIG.getBotVersion());

        //init api-builder
        LOGGER.info("initializing discord-api ...");

        discordApi = new DiscordApiBuilder()
                .setToken(CONFIG.get().getBot().getToken())
                .setIntents(Intent.GUILD_MESSAGE_REACTIONS, Intent.GUILD_MESSAGES, Intent.GUILD_VOICE_STATES)
                .login().join();

        LOGGER.info("discord login successful, continuing ... ");
        discordApi.addListener(new InteractionListener(this.discordApi));

        serverManager = new ServerManager();

        this.updatePresence();
        this.runTasks();
    }

    protected void runTasks() {
        LOGGER.info("starting tasks and commandline listener ...");

        executorPool.scheduleAtFixedRate(new CmdLineHandler(this), 0, 1, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler(this));
    }

    public void updatePresence() {
        discordApi.updateStatus(UserStatus.fromString(CONFIG.get().getBot().getStatus()));
    }

    public DiscordApi getAPI() {
        return this.discordApi;
    }

    public void shutdown() {
        MusicBot.LOGGER.info("shutting down ...");
        try {
            serverManager.shutdown();
            DATABASE.close();
            this.discordApi.disconnect().join();
            this.executorPool.shutdown();
        } catch (Exception e) {
            MusicBot.LOGGER.error(e);
        }
    }
}
