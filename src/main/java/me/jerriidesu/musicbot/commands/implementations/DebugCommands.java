package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
                            context.getSource().getLeft().getMessage().reply(
                                    MusicBot.getAudioManager()
                                            .getTrackManager(context.getSource().getRight())
                                            .getCurrentTrack()
                                            .getIdentifier()
                            );
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("position")
                        .executes(context -> {
                            //execute
                            context.getSource().getLeft().getMessage().reply(
                                    String.valueOf(MusicBot.getAudioManager()
                                            .getTrackManager(context.getSource().getRight())
                                            .getCurrentTrack()
                                            .getPosition()
                                    )
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
                ).then(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("version")
                        .executes(context -> {
                            context.getSource().getLeft().getMessage().reply(
                                    MusicBot.getConfig().getBotVersion()
                            );
                            return 1;
                        })
                ).then(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("system")
                        .executes(context -> {
                            SystemInfo sys = MusicBot.getSystemInfo();
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .addField("OS", sys.getOperatingSystem().getFamily() + " " + sys.getOperatingSystem().getVersionInfo().toString())
                                    .addField("CPU", sys.getHardware().getProcessor().getProcessorIdentifier().getName())
                                    .addField("RAM", (sys.getHardware().getMemory().getTotal() - sys.getHardware().getMemory().getAvailable()) + " / " + sys.getHardware().getMemory().getTotal());

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
