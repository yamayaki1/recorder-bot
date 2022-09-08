package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

public class VolumeCommand implements Command {
    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public void register(DiscordApi api) {
        SlashCommand.with(getName(), "Stellt die Lautstärke ein.")
                .setEnabledInDms(false)
                .addOption(SlashCommandOption.createDecimalOption("volume", "Lautstärke", true, 0.0, 120.0))
                .createGlobal(api).join();
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        int volume = either.getLeft().getOptionDecimalValueByName("volume").orElse(50.0).intValue();

        MusicBot.getAudioManager()
                .getTrackManager(either.getRight())
                .setVolume(volume);

        interUpdater.setContent("Lautstärke angepasst.").update();
    }
}
