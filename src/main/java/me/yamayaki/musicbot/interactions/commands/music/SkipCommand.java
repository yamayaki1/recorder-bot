package me.yamayaki.musicbot.interactions.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.ServerAudioPlayer;
import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SkipCommand implements ApplicationInteraction {
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

            List<AudioTrack> trackList = MusicBot.instance()
                    .getAudioManager(event.getAutocompleteInteraction().getServer().orElse(null))
                    .getPlaylist()
                    .getTracks(false);

            //Build Autocomplete list
            final List<SlashCommandOptionChoice> choices = new ArrayList<>();
            for (int i = 0; i < Math.min(24, trackList.size()); i++) {
                AudioTrackInfo trackInfo = trackList.get(i).getInfo();
                choices.add(SlashCommandOptionChoice.create(i + 1 + ". " + trackInfo.title + " - " + trackInfo.author.replaceAll("- Topic", ""), i + 1));
            }

            event.getAutocompleteInteraction().respondWithChoices(choices);
        });

        return builder;
    }

    @Override
    public void executeCommand(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater updater) {
        int amount = interaction.getArgumentDecimalValueByName("amount")
                .orElse(1.0).intValue();

        ServerAudioPlayer manager = MusicBot.instance()
                .getAudioManager(interaction.getServer().orElseThrow());

        int max_amount = manager.getPlaylist().getTracks(false).size() + 1;

        if (amount > max_amount || manager.hasFinished()) {
            String message = max_amount - 1 > 1
                    ? "Es k??nnen maximal " + max_amount + " Lieder ??bersprungen werden."
                    : "Es kann maximal ein Lied ??bersprungen werden.";
            updater.setContent(message).update();
            return;
        }

        MusicBot.instance()
                .getAudioManager(interaction.getServer().orElseThrow())
                .nextTrack(amount);

        updater.setContent(amount > 1 ? amount + " Lieder ??bersprungen." : "Ein Lied ??bersprungen.").update();
    }
}
