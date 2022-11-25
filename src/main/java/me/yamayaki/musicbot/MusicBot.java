package me.yamayaki.musicbot;

import me.yamayaki.musicbot.audio.ServerAudioManager;
import me.yamayaki.musicbot.database.RocksManager;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;
import me.yamayaki.musicbot.database.specs.impl.CacheSpecs;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.interactions.InteractionListener;
import me.yamayaki.musicbot.interactions.VoiceLeaveListener;
import me.yamayaki.musicbot.tasks.BackgroundTasks;
import me.yamayaki.musicbot.tasks.ShutdownHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.UserStatus;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MusicBot {
    public static final Logger LOGGER = LogManager.getLogger(MusicBot.class);
    public static final Config CONFIG = new Config(new File(".", "config/"));
    public static final RocksManager DATABASE = new RocksManager(new File(".", "database/"), new DatabaseSpec[]{
            CacheSpecs.SPOTIFY_CACHE,
            CacheSpecs.YOUTUBE_CACHE,
            CacheSpecs.PLAYLIST_CACHE,

            ChannelSpecs.CHANNEL_SETTINGS,
            ChannelSpecs.SERVER_PLAYERCHANNEL
    });
    private static MusicBot instance = null;
    private final ConcurrentHashMap<Server, ServerAudioManager> serverAudioManagers = new ConcurrentHashMap<>();

    private DiscordApi discordApi;

    public static void main(String[] args) {
        MusicBot bot = new MusicBot();
        bot.launch();
    }

    public static MusicBot instance() {
        if (instance == null) {
            throw new RuntimeException("instance not initialized!");
        }

        return instance;
    }

    protected void launch() {
        instance = this;

        //report version
        LOGGER.info("starting music-bot ({}) ...", Config.getVersion());

        //init api-builder
        LOGGER.info("initializing discord-api ({}) ...", Config.getDiscordVersion());

        discordApi = new DiscordApiBuilder()
                .setToken(CONFIG.getSetting("discord.token"))
                .setIntents(Intent.GUILD_VOICE_STATES)
                .login().join();

        LOGGER.info("discord login successful, continuing ... ");
        discordApi.setMessageCacheSize(0, 0);
        discordApi.addListener(new InteractionListener(this.discordApi));
        discordApi.addListener(new VoiceLeaveListener());

        this.updatePresence();
        this.runTasks();
    }

    protected void runTasks() {
        LOGGER.info("starting tasks and commandline listener ...");

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new BackgroundTasks(this), 0L, 1L, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler(this));
    }

    public void updatePresence() {
        discordApi.updateStatus(UserStatus.ONLINE);
        discordApi.updateActivity(ActivityType.PLAYING, "Recorder | " + Config.getVersion());
    }

    public DiscordApi getAPI() {
        return this.discordApi;
    }

    public ServerAudioManager getAudioManager(Server server) {
        return this.serverAudioManagers.computeIfAbsent(server, ServerAudioManager::new);
    }

    public void removeAudioManager(Server server) {
        this.serverAudioManagers.get(server).shutdown(false);
        this.serverAudioManagers.remove(server);
    }

    public ConcurrentHashMap<Server, ServerAudioManager> getAllAudioManagers() {
        return this.serverAudioManagers;
    }

    public void shutdown() {
        MusicBot.LOGGER.info("shutting down ...");
        try {
            // shutdown audiomanagers
            for (ServerAudioManager serverAudioManager : this.serverAudioManagers.values()) {
                serverAudioManager.shutdown(true);
            }

            DATABASE.close();
            this.discordApi.disconnect().join();
        } catch (Exception e) {
            MusicBot.LOGGER.error(e);
        }
    }
}
