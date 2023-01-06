package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.Config;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.entities.ChannelMessagePair;
import me.yamayaki.musicbot.entities.SpotifyTrack;
import me.yamayaki.musicbot.storage.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.utilities.CommonUtils;
import me.yamayaki.musicbot.utilities.RefreshableHolder;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PlayerControl {
    private final ServerAudioPlayer audioManager;
    private final RefreshableHolder<Message> controllerMessage;

    private boolean isDirty = true;

    public PlayerControl(ServerAudioPlayer audioManager) {
        this.audioManager = audioManager;
        this.controllerMessage = new RefreshableHolder<>((message) -> {
            if (message != null) {
                return message.getLatestInstance().join();
            }

            return null;
        });

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
            this.controllerMessage.setHoldable(
                    textChannel.get().getMessageById(pair.get().message()).join().getLatestInstance().join()
            );
        } catch (Exception e) {
            MusicBot.LOGGER.error(e);
        }
    }

    private void saveData() {
        this.controllerMessage.ifSetOrElse(message -> {
            MusicBot.LOGGER.info("saving playerchannel for server {}", this.audioManager.getServer().getName());
            MusicBot.DATABASE.getDatabase(ChannelSpecs.SERVER_PLAYERCHANNEL)
                    .putValue(this.audioManager.getServer().getId(), ChannelMessagePair.of(message));
        }, this::deleteData);
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

        this.controllerMessage.setHoldable(message);
        this.saveData();
        this.setDirty();

        return true;
    }

    public void setDirty() {
        this.isDirty = true;
    }

    public void updateMessage() {
        if (!this.isDirty) {
            return;
        }

        this.controllerMessage.ifSet(message -> {
            this.isDirty = false;

            MessageUpdater updater = message.createUpdater()
                    .setContent("")
                    .setEmbed(this.getEmbed())
                    .addComponents(this.getComponents());

            if (Config.isDevBuild()) {
                updater.addEmbed(this.getDebugEmbed());
            }

            updater.applyChanges().exceptionally(throwable -> {
                MusicBot.LOGGER.fatal(throwable);
                return null;
            });
        });
    }

    private ActionRow[] getComponents() {
        return new ActionRow[]{ActionRow.of(
                Button.primary("previous", "Vorheriges", "⏮️"),
                Button.primary("pause", "Pause", "⏯️"),
                Button.primary("stop", "Stop", "⏹️"),
                Button.primary("next", "Nächstes", "⏭️")
        ), ActionRow.of(
                Button.primary("vol_down", "Leise", "🔈"),
                Button.primary("vol_up", "Lauter", "🔊"),
                Button.primary("bass_toggle", "Bass-Boost", "\uD83C\uDD71")
        )};
    }

    private EmbedBuilder getEmbed() {
        AudioTrack currentTrack = this.audioManager.getPlayingTrack();
        AudioTrack nextTrack = this.audioManager.getPlaylist().next(false);

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (currentTrack != null) {
            SpotifyTrack spotifyData = currentTrack.getUserData(SpotifyTrack.class);

            embedBuilder.setImage(spotifyData != null ? spotifyData.image() : CommonUtils.getThumbnail(currentTrack.getIdentifier()))
                    .addField("Aktuelles Lied" + (this.audioManager.isPaused() ? " (pausiert)" : ""), currentTrack.getInfo().title + "\n" + currentTrack.getInfo().author.replaceAll("- Topic", ""), true);
        } else {
            embedBuilder.addField("Aktuelles Lied", "Aktuell spielt kein Lied!", true);
        }

        if (nextTrack != null) {
            embedBuilder.addField("Nächstes Lied", nextTrack.getInfo().title + "\n" + nextTrack.getInfo().author.replaceAll("- Topic", ""), true);
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

    private EmbedBuilder getDebugEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("-- Debug-Informationen --");

        String embedContent = "playlist.all: " + Arrays.toString(this.audioManager.getPlaylist().__dEntireList().stream().map(audioTrack -> audioTrack.getIdentifier() + "\n").toArray()) + "\n" +
                "player.volume: " + this.audioManager.getVolume() + "\n";

        embedBuilder.setDescription(embedContent);
        embedBuilder.setTimestampToNow();
        return embedBuilder;
    }

    public void onButtonClick(ButtonClickEvent event) {
        if (!event.getButtonInteraction().getMessage().equals(this.controllerMessage.getHoldable())) {
            return;
        }

        event.getButtonInteraction().acknowledge().join();

        switch (event.getButtonInteraction().getCustomId()) {
            case "stop" -> {
                this.audioManager.getPlaylist().clear();
                this.audioManager.stopTrack();
            }
            case "previous" -> this.audioManager.previousTrack(1);
            case "pause" -> this.audioManager.setPaused(!this.audioManager.isPaused());
            case "next" -> this.audioManager.nextTrack(1);
            case "vol_down" -> this.audioManager.setVolume(this.audioManager.getVolume() - 10);
            case "vol_up" -> this.audioManager.setVolume(this.audioManager.getVolume() + 10);
            case "bass_toggle" -> {
                if (Config.isDevBuild()) {
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
}
