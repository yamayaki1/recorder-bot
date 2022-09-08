package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.TrackManager;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.StringTools;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

public class PlayCommand implements Command {
    @Override
    public String getName() {
        return "play";
    }

    @Override
    public void register(DiscordApi api) {
        SlashCommand.with(getName(), "Fügt Musik der Playlist hinzu")
                .setEnabledInDms(false)
                .addOption(SlashCommandOption.createStringOption("query", "Spielt Musik von YouTube, Spotify, Twitch und ähnlichen Diensten ab.", true))
                .createGlobal(api).join();
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        TrackManager trackManager = MusicBot
                .getAudioManager()
                .getTrackManager(either.getRight());

        if (!trackManager.isConnected()) {
            interUpdater.setContent("Ich muss mit einem Sprachkanal verbunden sein. Bitte verwende vorher /connect").update();
            return;
        }

        String song = either.getLeft().getOptionStringValueByName("query").orElse("");
        if (!StringTools.isURL(song) && !song.contains("ytmsearch:") && !song.contains("ytsearch:")) {
            song = "ytmsearch:" + song;
        }

        trackManager.tryLoadItems(song, playerResponse -> {
            String message = playerResponse ? "Lied(er) erfolgreich geladen." : "Beim Laden der Lieder ist ein Fehler aufgetreten!";
            interUpdater.setContent(message).update();
        });
    }
}
