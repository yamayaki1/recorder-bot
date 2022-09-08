package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.TrackManager;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

public class SkipCommand implements Command {
    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public void register(DiscordApi api) {
        SlashCommand.with(getName(), "Überspringe Lieder in der Playlist.")
                .setEnabledInDms(false)
                .addOption(SlashCommandOption.createDecimalOption("amount", "Anzahl", true, 1.0, 20.0))
                .createGlobal(api).join();
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        int amount = either.getLeft().getOptionDecimalValueByName("amount")
                .orElse(1.0).intValue();

        TrackManager manager = MusicBot.getAudioManager()
                .getTrackManager(either.getRight());

        int max_amount = manager.getTracks().size();

        if (amount > max_amount || manager.hasFinished()) {
            interUpdater.setContent("Es können maximal " + max_amount + " Lieder übersprungen werden.").update();
            return;
        }

        MusicBot.getAudioManager()
                .getTrackManager(either.getRight())
                .skipTrack(amount);

        interUpdater.setContent(amount + " Lieder übersprungen.").update();
    }
}
