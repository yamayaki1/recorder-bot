package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.audio.TrackManager;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Either;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import oshi.SystemInfo;

public class DebugCommands implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("debug")
                .then(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("current")
                        .executes(context -> {
                            //execute
                            AudioTrack audioTrack = MusicBot.getAudioManager()
                                    .getTrackManager(context.getSource().getRight())
                                    .getCurrentTrack();

                            if (audioTrack == null) {
                                return 1;
                            }

                            context.getSource().getLeft().getMessage().reply(
                                    new EmbedBuilder().setTitle("track")
                                            .addField("title", audioTrack.getInfo().title)
                                            .addField("author", audioTrack.getInfo().author)
                                            .addField("source", audioTrack.getSourceManager().getSourceName())
                                            .addField("identifier", audioTrack.getIdentifier())
                            );

                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("fix")
                        .executes(context -> {
                            //execute
                            MusicBot.getAudioManager()
                                    .getTrackManager(context.getSource().getRight())
                                    .fixAudioSource();
                            return 1;
                        })
                ).then(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("about")
                        .executes(context -> {
                            SystemInfo sys = MusicBot.getSystemInfo();
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .addField("Version", MusicBot.getConfig().getBotVersion())
                                    .addField("Repository", "https://github.com/jerriidesu/musicbot")
                                    .addField("Entwickler", "Yamayaki (<@310370479380627458>)")
                                    .setThumbnail("https://avatars.githubusercontent.com/u/65787801?s=100")
                                    .addField("OS", sys.getOperatingSystem().getFamily() + " " + sys.getOperatingSystem().getVersionInfo().toString())
                                    .addField("CPU", sys.getHardware().getProcessor().getProcessorIdentifier().getName() + " (" + sys.getHardware().getProcessor().getMaxFreq() / 1000000 + " MHz)")
                                    .addField("RAM", ((sys.getHardware().getMemory().getTotal() / 1048576) - (sys.getHardware().getMemory().getAvailable() / 1048576)) + "MB / " + (sys.getHardware().getMemory().getTotal() / 1048576) + "MB");

                            context.getSource().getLeft().getMessage().reply(embedBuilder);
                            return 1;
                        })
                ).then(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("last_errors")
                        .executes(context -> {
                            EmbedBuilder embedBuilder = new EmbedBuilder();

                            for (TrackManager manager : MusicBot.getAudioManager().getAll()) {
                                embedBuilder.addField(manager.getServerName(), manager.lastError);
                            }

                            context.getSource().getLeft().getMessage().reply(embedBuilder);
                            return 1;
                        })
                ).executes(context -> {
                    Reactions.addRefuseReaction(context.getSource().getLeft().getMessage());
                    return 1;
                })
        );
    }
}
