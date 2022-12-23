package me.yamayaki.musicbot.interactions.commands.music;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.ServerAudioManager;
import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import me.yamayaki.musicbot.utilities.CommonUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.Optional;

public class PlayCommand implements ApplicationInteraction {
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
    public void executeCommand(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater updater) {
        Optional<ServerVoiceChannel> userChannel = interaction.getUser()
                .getConnectedVoiceChannel(interaction.getServer().orElseThrow());

        if (userChannel.isEmpty()) {
            updater.setContent("Beim Beitreten des Sprachkanals ist ein Fehler aufgetreten. Befindest du dich in einem Kanal?").update();
            return;
        }

        interaction.getServer().orElseThrow().getAudioConnection().ifPresentOrElse(audioConnection -> {
            if (!audioConnection.getChannel().equals(userChannel.get())) {
                userChannel.get().connect(false, false).join();
            }
        }, () -> userChannel.get().connect(false, false).join());

        String song = interaction.getArgumentStringValueByName("query").orElse("");
        if (!CommonUtils.isURL(song) && !song.contains("ytmsearch:") && !song.contains("ytsearch:")) {
            song = "ytmsearch:" + song;
        }

        ServerAudioManager serverAudioManager = MusicBot.instance()
                .getAudioManager(interaction.getServer().orElseThrow());

        serverAudioManager.tryLoadItems(song, playerResponse -> {
            String message;
            if (playerResponse.isSuccess()) {
                message = playerResponse.getCount() > 1
                        ? "Es wurden " + playerResponse.getCount() + " Lieder der Playlist hinzugefügt."
                        : "Das Lied **" + playerResponse.getTitle() + "** wurde der Playlist hinzugefügt.";
            } else {
                message = "Beim Laden der Lieder ist ein Fehler aufgetreten.";
            }

            updater.setContent(message).update();
        });
    }
}
