package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.Config;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.entities.ChannelMessagePair;
import me.yamayaki.musicbot.entities.SpotifyTrack;
import me.yamayaki.musicbot.storage.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.utilities.CommonUtils;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PlayerControl {
    private final ServerAudioManager audioManager;
    private final Holder controllerMessage = new Holder();

    private boolean isDirty = true;

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

        Optional<ServerTextChannel> textChannel = this.audioManager.getServer().getTextChannelById(pair.get().channel());

        if (textChannel.isEmpty()) {
            return;
        }

        try {
            this.controllerMessage.setMessage(
                    textChannel.get().getMessageById(pair.get().message()).join().getLatestInstance().join()
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
                .putValue(this.audioManager.getServer().getId(), ChannelMessagePair.of(this.controllerMessage.getMessage()));
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
        this.setDirty();

        return true;
    }

    public void setDirty() {
        this.isDirty = true;
    }

    public void updateMessage() {
        if (!this.controllerMessage.isSet() || !this.isDirty) {
            return;
        }

        this.isDirty = false;

        this.controllerMessage.getMessage().createUpdater()
                .setContent("")
                .setEmbed(this.getEmbed())
                .addComponents(this.getComponents())
                .applyChanges().join();
    }

    private ActionRow[] getComponents() {
        return new ActionRow[]{ActionRow.of(
                Button.danger("stop", "", "⏹️"),
                Button.primary("pause", "", "⏯️"),
                Button.primary("skip", "", "⏭️")
        ), ActionRow.of(
                Button.primary("vol_down", "", "🔈"),
                Button.primary("vol_up", "", "🔊"),
                Button.primary("bass_toggle", "", "\uD83C\uDD71")
        )};
    }

    private EmbedBuilder getEmbed() {
        AudioTrack currentTrack = this.audioManager.getPlayingTrack();
        AudioTrack nextTrack = this.audioManager.getPlaylist().peekNext();

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (currentTrack != null) {
            SpotifyTrack spotifyData = currentTrack.getUserData(SpotifyTrack.class);

            embedBuilder.setThumbnail(spotifyData != null ? spotifyData.image() : CommonUtils.getThumbnail(currentTrack.getIdentifier()))
                    .addField("Aktuelles Lied" + (this.audioManager.isPaused() ? " (pausiert)" : ""), currentTrack.getInfo().title + "\n" + currentTrack.getInfo().author.replaceAll("- Topic", ""));
        } else {
            embedBuilder.addField("Aktuelles Lied", "Aktuell Spielt kein Lied!");
        }

        if (nextTrack != null) {
            embedBuilder.addField("Nächstes Lied", nextTrack.getInfo().title + "\n" + nextTrack.getInfo().author.replaceAll("- Topic", ""));
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

        event.getButtonInteraction().acknowledge().join();

        switch (event.getButtonInteraction().getCustomId()) {
            case "stop" -> {
                this.audioManager.getPlaylist().clear();
                this.audioManager.stopTrack();
            }
            case "pause" -> this.audioManager.setPaused(!this.audioManager.isPaused());
            case "skip" -> this.audioManager.skipTrack(1);
            case "vol_down" -> this.audioManager.setVolume(this.audioManager.getVolume() - 15);
            case "vol_up" -> this.audioManager.setVolume(this.audioManager.getVolume() + 15);
            case "bass_toggle" -> {
                if(Config.isDevBuild()) {
                    this.audioManager.toggleBassboost();
                } else {
                    event.getButtonInteraction().createFollowupMessageBuilder()
                            .setFlags(MessageFlag.EPHEMERAL)
                            .setContent("Dieser Bot befindet sich nicht im Entwicklungsmodus! Es können daher auch keine Features in Entwicklung verwendet werden.")
                            .send();
                }
            }
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
