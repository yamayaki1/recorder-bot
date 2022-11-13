package me.yamayaki.musicbot.interactions.commands.music;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.TrackManager;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.ChannelUtilities;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.StringTools;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

public class PlayCommand implements Command {
    @Override
    public String getName() {
        return "play";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Fügt Musik der Playlist hinzu")
                .setEnabledInDms(false)
                .addOption(
                        SlashCommandOption.createStringOption("query", "Spielt Musik von YouTube, Spotify, Twitch und ähnlichen Diensten ab.", true)
                );
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        TrackManager trackManager = MusicBot
                .getAudioManager()
                .getTrackManager(either.getRight());

        ChannelUtilities.joinVoiceChannel(either, () -> {
            String song = either.getLeft().getArgumentStringValueByName("query").orElse("");
            if (!StringTools.isURL(song) && !song.contains("ytmsearch:") && !song.contains("ytsearch:")) {
                song = "ytmsearch:" + song;
            }

            trackManager.tryLoadItems(song, playerResponse -> {
                String message;
                if (playerResponse.isSuccess()) {
                    message = playerResponse.getCount() > 1
                            ? "Es wurden " + playerResponse.getCount() + " Lieder der Playlist hinzugefügt."
                            : "Ein Lied der Playlist hinzugefügt.";
                } else {
                    message = "Beim Laden der Lieder ist ein Fehler aufgetreten: " + playerResponse.getMessage();
                }

                interUpdater.setContent(message).update();
            });
        }, () -> interUpdater.setContent("Beim Beitreten des Sprachkanals ist ein Fehler aufgetreten. Befindest du dich in einem Kanal?").update());
    }
}
