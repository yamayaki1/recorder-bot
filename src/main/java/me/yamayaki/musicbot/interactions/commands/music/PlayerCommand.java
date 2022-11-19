package me.yamayaki.musicbot.interactions.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.YouTubeUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

public class PlayerCommand implements Command {
    private final String CMD_PAUSE = "pause";
    private final String CMD_RESUME = "resume";
    private final String CMD_LOOP = "loop";
    private final String CMD_CURRENT = "current";

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Ändere Einstellungen des Players.")
                .setEnabledInDms(false)
                .addOption(SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "action", "Aktion", true,
                        new SlashCommandOptionChoiceBuilder().setName("Aktuelles Lied").setValue(CMD_CURRENT),
                        new SlashCommandOptionChoiceBuilder().setName("Pausieren").setValue(CMD_PAUSE),
                        new SlashCommandOptionChoiceBuilder().setName("Fortsetzen").setValue(CMD_RESUME),
                        new SlashCommandOptionChoiceBuilder().setName("Wiederholen").setValue(CMD_LOOP)
                ));
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();

        switch (either.getLeft().getArgumentStringValueByName("action").orElse("")) {
            case CMD_PAUSE -> {
                MusicBot.instance()
                        .getAudioManager(either.getRight())
                        .setPaused(true);

                interUpdater.setContent("Aktuelles Lied pausiert.").update();
            }
            case CMD_RESUME -> {
                MusicBot.instance()
                        .getAudioManager(either.getRight())
                        .startPlaying();

                interUpdater.setContent("Lied fortgesetzt.").update();
            }
            case CMD_LOOP -> {
                boolean repeat = MusicBot.instance()
                        .getAudioManager(either.getRight())
                        .getPlaylist()
                        .toggleRepeat();

                interUpdater.setContent(repeat ? "Wiederholen eingeschaltet." : "Wiederholen ausgeschaltet.").update();
            }
            case CMD_CURRENT -> {
                var trackManager = MusicBot.instance()
                        .getAudioManager(either.getRight());

                if (trackManager.hasFinished() || trackManager.getPlaylist().current() == null) {
                    interUpdater.setContent("Es spielt aktuell kein Lied.").update();
                    return;
                }

                AudioTrack audioTrack = trackManager.getPlaylist()
                        .current();

                SpotifyTrack spotifyData = audioTrack.getUserData(SpotifyTrack.class);

                EmbedBuilder replyEmbed = new EmbedBuilder()
                        .setImage(spotifyData != null ? spotifyData.getImage() : YouTubeUtils.getThumbnail(audioTrack.getIdentifier()))
                        .addField("Titel", audioTrack.getInfo().title)
                        .addField("Künstler", audioTrack.getInfo().author)
                        .setTimestampToNow();

                interUpdater.addEmbed(replyEmbed).update();
            }
            default -> interUpdater.setContent("Unbekannte Aktion").update();
        }
    }
}
