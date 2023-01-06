package me.yamayaki.musicbot.interactions.commands.music;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.List;

public class PlayerCommand implements ApplicationInteraction {
    private final String CMD_PAUSE = "pause";
    private final String CMD_RESUME = "resume";
    private final String CMD_VOLUME = "volume";

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Ändere Einstellungen des Players.")
                .setEnabledInDms(false)
                .addOption(SlashCommandOption.createSubcommand("action", "Aktion", List.of(
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "action", "Aktion", true,
                                new SlashCommandOptionChoiceBuilder().setName("Pausieren").setValue(CMD_PAUSE),
                                new SlashCommandOptionChoiceBuilder().setName("Fortsetzen").setValue(CMD_RESUME)
                        )
                ))).addOption(SlashCommandOption.createSubcommand("volume", "Lautstärke", List.of(
                        SlashCommandOption.createDecimalOption(CMD_VOLUME, "Lautstärke", true)
                )));
    }

    @Override
    public void executeCommand(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater updater) {
        switch (interaction.getArgumentStringValueByName("action").orElse("")) {
            case CMD_PAUSE -> {
                MusicBot.instance()
                        .getAudioManager(interaction.getServer().orElseThrow())
                        .setPaused(true);

                updater.setContent("Aktuelles Lied pausiert.").update();
            }
            case CMD_RESUME -> {
                MusicBot.instance()
                        .getAudioManager(interaction.getServer().orElseThrow())
                        .startPlaying();

                updater.setContent("Lied fortgesetzt.").update();
            }
            case CMD_VOLUME -> {
                int volume = interaction.getArgumentDecimalValueByName("volume").orElse(50.0).intValue();

                MusicBot.instance()
                        .getAudioManager(interaction.getServer().orElseThrow())
                        .setVolume(volume);

                updater.setContent("Lautstärke angepasst.").update();
            }
            default -> updater.setContent("Unbekannte Aktion").update();
        }
    }
}
