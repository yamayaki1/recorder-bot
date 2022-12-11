package me.yamayaki.musicbot.interactions.commands.music;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.ServerAudioManager;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.ChannelUtilities;
import me.yamayaki.musicbot.utils.StringTools;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

import java.util.Optional;

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
    public void execute(SlashCommandInteraction interaction) {
        var interUpdater = interaction.respondLater(true).join();

        Optional<ServerVoiceChannel> userChannel = interaction.getUser()
                .getConnectedVoiceChannel(interaction.getServer().orElseThrow());

        if(userChannel.isEmpty()) {
            interUpdater.setContent("Beim Beitreten des Sprachkanals ist ein Fehler aufgetreten. Befindest du dich in einem Kanal?").update();
            return;
        }

        userChannel.get().connect(false, false);

        String song = interaction.getArgumentStringValueByName("query").orElse("");
        if (!StringTools.isURL(song) && !song.contains("ytmsearch:") && !song.contains("ytsearch:")) {
            song = "ytmsearch:" + song;
        }

        ServerAudioManager serverAudioManager = MusicBot.instance()
                .getAudioManager(interaction.getRegisteredCommandServer().orElseThrow());

        serverAudioManager.tryLoadItems(song, playerResponse -> {
            String message;
            if (playerResponse.isSuccess()) {
                message = playerResponse.getCount() > 1
                        ? "Es wurden " + playerResponse.getCount() + " Lieder der Playlist hinzugefügt."
                        : "Das Lied **" + playerResponse.getTitle() + "** wurde der Playlist hinzugefügt.";
            } else {
                message = "Beim Laden der Lieder ist ein Fehler aufgetreten: " + playerResponse.getMessage();
            }

            interUpdater.setContent(message).update();
        });
    }
}
