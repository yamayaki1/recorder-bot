package me.yamayaki.musicbot.interactions.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.TrackManager;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SkipCommand implements Command {
    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        var builder = SlashCommand.with(getName(), "Zu einem Lied in der Playlist springen.")
                .setEnabledInDms(false)
                .addOption(
                        SlashCommandOption.createDecimalOption("amount", "Anzahl", true, true)
                );

        //Add Autocomplete listener
        api.addAutocompleteCreateListener(event -> {
            if (!Objects.equals(getName(), event.getAutocompleteInteraction().getCommandName())) {
                return;
            }

            List<AudioTrack> trackList = MusicBot.getAudioManager()
                    .getTrackManager(event.getAutocompleteInteraction().getServer().orElse(null))
                    .getPlaylist()
                    .getTracks(false);

            //Build Autocomplete list
            final List<SlashCommandOptionChoice> choices = new ArrayList<>();
            for (int i = 0; i < trackList.size(); i++) {
                AudioTrackInfo trackInfo = trackList.get(i).getInfo();
                choices.add(SlashCommandOptionChoice.create(i + 1 + ". " + trackInfo.title + " - " + trackInfo.author.replaceAll("- Topic", ""), i + 1));
            }

            event.getAutocompleteInteraction().respondWithChoices(choices);
        });

        return builder;
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        int amount = either.getLeft().getOptionDecimalValueByName("amount")
                .orElse(1.0).intValue();

        TrackManager manager = MusicBot.getAudioManager()
                .getTrackManager(either.getRight());

        int max_amount = manager.getPlaylist().getTracks(false).size() + 1;

        if (amount > max_amount || manager.hasFinished()) {
            interUpdater.setContent("Es können maximal " + (max_amount - 1) + " Lieder übersprungen werden.").update();
            return;
        }

        MusicBot.getAudioManager()
                .getTrackManager(either.getRight())
                .skipTrack(amount);

        interUpdater.setContent(amount + " Lieder übersprungen.").update();
    }
}
