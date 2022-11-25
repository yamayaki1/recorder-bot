package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.entities.LoaderResponse;
import me.yamayaki.musicbot.audio.player.LavaAudioSource;
import me.yamayaki.musicbot.audio.player.LavaManager;
import me.yamayaki.musicbot.audio.player.LoadResultHandler;
import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.utils.ChannelMessagePair;
import me.yamayaki.musicbot.utils.YouTubeUtils;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import java.util.Optional;
import java.util.function.Consumer;

public class ServerAudioManager extends AudioEventAdapter {
    private final Server server;

    private final LavaAudioSource audioSource;
    private final PlaylistManager playlist;

    private boolean skipping = false;
    private Optional<Message> playerMessage;

    private long lastActiveTime = System.currentTimeMillis();

    public ServerAudioManager(Server server) {
        this.server = server;
        this.audioSource = new LavaAudioSource(server.getApi(), this);
        this.playlist = new PlaylistManager(this.server.getId());

        this.playerMessage = this.loadPlayerMessage();
        this.startPlaying();
    }

    public void tryLoadItems(String song, Consumer<LoaderResponse> consumer) {
        try {
            LavaManager.loadTrack(song, new LoadResultHandler(this.getPlaylist(), 0L, consumer)).get();
            this.startPlaying();
        } catch (Exception ignored) {
        }
    }

    public void skipTrack(int count) {
        this.skipping = true;

        for (int i = 0; i < Math.max(count, 1); i++) {
            this.audioSource.getAudioPlayer().stopTrack();
            this.startPlaying();
        }

        this.skipping = false;
    }

    public void startPlaying() {
        this.setPaused(false);

        if (this.audioSource.hasFinished() && this.playlist.hasNext()) {
            this.audioSource.getAudioPlayer()
                    .playTrack(this.playlist.next());
        }

        this.fixAudioSource();
    }

    public boolean hasFinished() {
        return this.audioSource
                .hasFinished();
    }

    public boolean isPaused() {
        return this.audioSource.getAudioPlayer()
                .isPaused();
    }

    public void setPaused(boolean bool) {
        this.audioSource.getAudioPlayer()
                .setPaused(bool);
    }

    public void setVolume(int volume) {
        this.audioSource.getAudioPlayer()
                .setVolume(volume);
    }

    public void fixAudioSource() {
        this.server.getAudioConnection().ifPresent(audioConnection -> {
            audioConnection.setAudioSource(this.audioSource);
            MusicBot.LOGGER.debug("Setting audio source for connection in {}", this.server.getId());
        });
    }

    public PlaylistManager getPlaylist() {
        return this.playlist;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.updateMessage();
        this.lastActiveTime = System.currentTimeMillis();

        if (!(this.getPlaylist().hasNext() || endReason.mayStartNext)) {
            return;
        }

        if(!this.skipping) {
            this.startPlaying();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.updateMessage();
        this.lastActiveTime = System.currentTimeMillis();
    }

    public boolean isInactive() {
        return this.audioSource.hasFinished() && this.lastActiveTime < (System.currentTimeMillis() - 3E5);
    }


    public boolean setPlayerChannel(ServerChannel channel) {
        this.loadPlayerMessage().ifPresent(Deletable::delete);

        if(channel.asServerTextChannel().isEmpty()) {
            return false;
        }

        Message message = channel.asServerTextChannel().get().sendMessage("PLAYER_EMBED").join();

        if(message == null) {
            return false;
        }

        this.playerMessage = Optional.of(message);
        this.updateMessage();
        this.savePlayerMessage();

        return true;
    }

    private void updateMessage() {
        if(this.skipping) {
            return;
        }

        playerMessage.ifPresent(message -> message.createUpdater()
                .setContent("")
                .setEmbed(this.getEmbed())
                .applyChanges().join()
        );
    }

    private EmbedBuilder getEmbed() {
        AudioTrack currentTrack = this.playlist.current();
        AudioTrack nextTrack = this.playlist.peekNext();

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if(currentTrack != null) {
            SpotifyTrack spotifyData = currentTrack.getUserData(SpotifyTrack.class);

            embedBuilder.setThumbnail(spotifyData != null ? spotifyData.getImage() : YouTubeUtils.getThumbnail(currentTrack.getIdentifier()))
                    .addField("Aktuelles Lied", currentTrack.getInfo().title + "\n"+currentTrack.getInfo().author);
        } else {
            embedBuilder.addField("Aktuelles Lied", "-- \n --");
        }

        if(nextTrack != null) {
            embedBuilder.addField("Nächstes Lied", nextTrack.getInfo().title + "\n"+nextTrack.getInfo().author);
        }

        embedBuilder.setTimestampToNow();
        return embedBuilder;
    }

    private Optional<Message> loadPlayerMessage() {
        Optional<ChannelMessagePair> pair = MusicBot.DATABASE.getDatabase(ChannelSpecs.SERVER_PLAYERCHANNEL)
                .getValue(this.server.getId());

        if(pair.isEmpty()) {
            return Optional.empty();
        }

        Optional<ServerTextChannel> textChannel = this.server.getTextChannelById(pair.get().channelId());

        if(textChannel.isEmpty()) {
            return Optional.empty();
        }

        Message message = textChannel.get().getMessageById(pair.get().messageId()).join();
        return Optional.ofNullable(message);
    }

    private void savePlayerMessage() {
        this.playerMessage.ifPresentOrElse(message -> {
            MusicBot.LOGGER.debug("saving playerchannel for server {}", this.server.getName());
            MusicBot.DATABASE.getDatabase(ChannelSpecs.SERVER_PLAYERCHANNEL)
                    .putValue(this.server.getId(), new ChannelMessagePair(message.getChannel().getId(), message.getId()));
        }, () -> {
            MusicBot.LOGGER.debug("deleting playerchannel for server {}", this.server.getName());
            MusicBot.DATABASE.getDatabase(ChannelSpecs.SERVER_PLAYERCHANNEL)
                    .deleteValue(this.server.getId());
        });
    }

    public void shutdown(boolean save) {
        this.audioSource.getAudioPlayer().destroy();
        this.savePlayerMessage();
        this.server.getAudioConnection().ifPresent(audioConnection -> {
            audioConnection.removeAudioSource();
            audioConnection.getChannel().disconnect();
        });

        if (save) {
            this.playlist.store();
        }
    }
}
