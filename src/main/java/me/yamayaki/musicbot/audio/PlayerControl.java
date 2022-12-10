package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.utils.ChannelMessagePair;
import me.yamayaki.musicbot.utils.YouTubeUtils;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PlayerControl {
    private final ServerAudioManager audioManager;
    private final Holder controllerMessage = new Holder();

    public PlayerControl(ServerAudioManager audioManager) {
        this.audioManager = audioManager;
        this.loadData();
    }

    private void loadData() {
        Optional<ChannelMessagePair> pair = MusicBot.DATABASE.getDatabase(ChannelSpecs.SERVER_PLAYERCHANNEL)
                .getValue(this.audioManager.getServer().getId());

        if (pair.isEmpty()) {
            return;
        }

        Optional<ServerTextChannel> textChannel = this.audioManager.getServer().getTextChannelById(pair.get().channelId());

        if (textChannel.isEmpty()) {
            return;
        }

        try {
            this.controllerMessage.setMessage(
                    textChannel.get().getMessageById(pair.get().messageId()).join().getLatestInstance().join()
            );
        } catch (Exception e) {
            MusicBot.LOGGER.error(e);
        }
    }

    private void saveData() {
        if (!this.controllerMessage.isSet()) {
            this.deleteData();
            return;
        }

        MusicBot.LOGGER.info("saving playerchannel for server {}", this.audioManager.getServer().getName());
        MusicBot.DATABASE.getDatabase(ChannelSpecs.SERVER_PLAYERCHANNEL)
                .putValue(this.audioManager.getServer().getId(), new ChannelMessagePair(this.controllerMessage.getMessage().getChannel().getId(), this.controllerMessage.getMessage().getId()));
    }

    private void deleteData() {
        this.controllerMessage.ifSet(Deletable::delete);

        MusicBot.LOGGER.info("deleting playerchannel for server {}", this.audioManager.getServer().getName());
        MusicBot.DATABASE.getDatabase(ChannelSpecs.SERVER_PLAYERCHANNEL)
                .deleteValue(this.audioManager.getServer().getId());
    }

    public boolean setPlayerChannel(ServerChannel channel) {
        this.deleteData();

        if (channel.asServerTextChannel().isEmpty()) {
            return false;
        }

        Message message = channel.asServerTextChannel().get().sendMessage("PLAYER_EMBED").join();

        if (message == null) {
            return false;
        }

        this.controllerMessage.setMessage(message);
        this.saveData();
        this.updateMessage();

        return true;
    }

    public void updateMessage() {
        if (!this.controllerMessage.isSet()) {
            return;
        }

        try {
            this.controllerMessage.getMessage().createUpdater()
                    .setContent("")
                    .setEmbed(this.getEmbed())
                    .addComponents(this.getComponents())
                    .applyChanges().join();
        } catch (Exception e) {
            MusicBot.LOGGER.error(e);
        }
    }

    private ActionRow getComponents() {
        return ActionRow.of(
                //Button.success("add", "+"),
                Button.danger("stop", "", "⏹️"),
                Button.primary("pause", "", "⏯️"),
                Button.primary("skip", "", "⏭️"),
                Button.primary("vol_down", "", "🔈"),
                Button.primary("vol_up", "", "🔊")
        );
    }

    private EmbedBuilder getEmbed() {
        AudioTrack currentTrack = this.audioManager.getPlayingTrack();
        AudioTrack nextTrack = this.audioManager.getPlaylist().peekNext();

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (currentTrack != null) {
            SpotifyTrack spotifyData = currentTrack.getUserData(SpotifyTrack.class);

            embedBuilder.setThumbnail(spotifyData != null ? spotifyData.getImage() : YouTubeUtils.getThumbnail(currentTrack.getIdentifier()))
                    .addField("Aktuelles Lied" + (this.audioManager.isPaused() ? " (pausiert)" : ""), currentTrack.getInfo().title + "\n" + currentTrack.getInfo().author);
        } else {
            embedBuilder.addField("Aktuelles Lied", "Aktuell Spielt kein Lied!");
        }

        if (nextTrack != null) {
            embedBuilder.addField("Nächstes Lied", nextTrack.getInfo().title + "\n" + nextTrack.getInfo().author);
        }

        List<AudioTrack> list = this.audioManager.getPlaylist().getTracks(false);
        if (list.size() > 0) {
            embedBuilder.addField("Warteschlange", list.size() > 1 ? list.size() + " Lieder" : "Ein Lied");
        } else {
            embedBuilder.addField("Warteschlange", "Keine Lieder");
        }

        embedBuilder.setTimestampToNow();
        return embedBuilder;
    }

    public void onButtonClick(ButtonClickEvent event) {
        if (!event.getButtonInteraction().getMessage().equals(this.controllerMessage.getMessage())) {
            return;
        }

        switch (event.getButtonInteraction().getCustomId()) {
            case "add" ->
                    event.getButtonInteraction().respondWithModal("add_song", "Lied oder Playlist hinzufügen.", ActionRow.of(
                            TextInput.create(
                                    TextInputStyle.SHORT, "query", "Link zum Lied oder zur Playlist", true
                            )
                    )).join();
            case "stop" -> {
                this.audioManager.getPlaylist().clear();
                this.audioManager.stopTrack();
                event.getButtonInteraction().acknowledge();
            }
            case "pause" -> {
                this.audioManager.setPaused(!this.audioManager.isPaused());
                event.getButtonInteraction().acknowledge();
            }
            case "skip" -> {
                this.audioManager.skipTrack(1);
                event.getButtonInteraction().acknowledge();
            }
            case "vol_down" -> {
                this.audioManager.setVolume(this.audioManager.getVolume()-15);
                event.getButtonInteraction().acknowledge();
            }
            case "vol_up" -> {
                this.audioManager.setVolume(this.audioManager.getVolume()+15);
                event.getButtonInteraction().acknowledge();
            }
            default -> event.getButtonInteraction().acknowledge();
        }
    }

    public void shutdown() {
        this.saveData();
        this.controllerMessage.ifSet(message -> {
            message.createUpdater().removeAllComponents().applyChanges();
        });
    }

    public static class Holder {
        private Message message;

        public Holder() {
            this.message = null;
        }

        public void ifSet(Consumer<Message> consumer) {
            this.update();
            if (this.message != null) {
                consumer.accept(this.message);
            }
        }

        public boolean isSet() {
            this.update();
            return this.message != null;
        }

        public Message getMessage() {
            this.update();
            return this.message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public void update() {
            if (this.message == null) {
                return;
            }

            try {
                this.message = this.message.getLatestInstance().join();
            } catch (Exception e) {
                this.message = null;
                MusicBot.LOGGER.error(e);
            }
        }
    }
}
