package me.yamayaki.musicbot.interactions.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.TrackManager;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

public class DebugCommands implements Command {
    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.withRequiredPermissions(getName(), "Befehle für die Botentwicklung", PermissionType.ADMINISTRATOR)
                .setEnabledInDms(false)
                .addOption(SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "subcommand", "Unterbefehl", true,
                        new SlashCommandOptionChoiceBuilder().setName("Über den Bot").setValue("about"),
                        new SlashCommandOptionChoiceBuilder().setName("Aktueller Song").setValue("current_song"),
                        new SlashCommandOptionChoiceBuilder().setName("Ausgabe fixen").setValue("reconnect"),
                        new SlashCommandOptionChoiceBuilder().setName("Letzte Fehler").setValue("last_errors")
                ));
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();

        switch (either.getLeft().getOptionStringValueByName("subcommand").orElse("")) {
            case "about" -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .addField("Version", MusicBot.CONFIG.getBotVersion())
                        .addField("Repository", "https://github.com/yamayaki1/recorder-bot", true)
                        .addField("Entwickler", "Yamayaki (<@310370479380627458>)", true)
                        .addField("Betriebssystem", String.format("%s (%s) %s", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version")), true)
                        .addField("Arbeitsspeicher", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "/" + (Runtime.getRuntime().totalMemory() / 1048576) + "MB", true);

                interUpdater.addEmbed(embedBuilder).update();
            }
            case "current_song" -> {
                AudioTrack audioTrack = MusicBot.getAudioManager()
                        .getTrackManager(either.getRight())
                        .getPlaylist()
                        .getCurrentTrack();

                if (audioTrack == null) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("track")
                        .addField("title", audioTrack.getInfo().title)
                        .addField("author", audioTrack.getInfo().author)
                        .addField("source", audioTrack.getSourceManager().getSourceName())
                        .addField("url", audioTrack.getInfo().uri);

                interUpdater.addEmbed(embedBuilder).update();
            }
            case "reconnect" -> {
                var optChannel = either.getLeft().getUser()
                        .getConnectedVoiceChannel(either.getRight());

                optChannel.ifPresent(serverVoiceChannel -> {
                    var audioConnection = serverVoiceChannel.connect().join();
                    audioConnection.setSelfDeafened(true);
                });

                MusicBot.getAudioManager()
                        .getTrackManager(either.getRight())
                        .fixAudioSource();

                interUpdater.setContent("Audioconnection wiederhergestellt").update();
            }
            case "last_errors" -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();

                for (TrackManager manager : MusicBot.getAudioManager().getAll()) {
                    embedBuilder.addField(manager.getServerName(), manager.lastError);
                }

                interUpdater.addEmbed(embedBuilder).update();
            }
            default -> interUpdater.setContent("Unbekannter Unterbefehl").update();
        }
    }
}
