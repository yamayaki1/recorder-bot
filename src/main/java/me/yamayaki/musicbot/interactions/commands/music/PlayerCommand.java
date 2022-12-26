package me.yamayaki.musicbot.interactions.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.entities.SpotifyTrack;
import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import me.yamayaki.musicbot.utilities.CommonUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
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
    private final String CMD_LOOP = "loop";
    private final String CMD_CURRENT = "current";
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
                                new SlashCommandOptionChoiceBuilder().setName("Aktuelles Lied").setValue(CMD_CURRENT),
                                new SlashCommandOptionChoiceBuilder().setName("Pausieren").setValue(CMD_PAUSE),
                                new SlashCommandOptionChoiceBuilder().setName("Fortsetzen").setValue(CMD_RESUME),
                                new SlashCommandOptionChoiceBuilder().setName("Wiederholen").setValue(CMD_LOOP)
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
            case CMD_LOOP -> {
                boolean repeat = MusicBot.instance()
                        .getAudioManager(interaction.getServer().orElseThrow())
                        .getPlaylist()
                        .toggleRepeat();

                updater.setContent(repeat ? "Wiederholen eingeschaltet." : "Wiederholen ausgeschaltet.").update();
            }
            case CMD_CURRENT -> {
                var trackManager = MusicBot.instance()
                        .getAudioManager(interaction.getServer().orElseThrow());

                if (trackManager.hasFinished() || trackManager.getPlaylist().current() == null) {
                    updater.setContent("Es spielt aktuell kein Lied.").update();
                    return;
                }

                AudioTrack audioTrack = trackManager.getPlaylist()
                        .current();

                SpotifyTrack spotifyData = audioTrack.getUserData(SpotifyTrack.class);

                EmbedBuilder replyEmbed = new EmbedBuilder()
                        .setImage(spotifyData != null ? spotifyData.image() : CommonUtils.getThumbnail(audioTrack.getIdentifier()))
                        .addField("Titel", audioTrack.getInfo().title)
                        .addField("Künstler", audioTrack.getInfo().author)
                        .setTimestampToNow();

                updater.addEmbed(replyEmbed).update();
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
