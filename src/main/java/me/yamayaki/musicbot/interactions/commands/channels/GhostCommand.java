package me.yamayaki.musicbot.interactions.commands.channels;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GhostCommand implements Command {
    @Override
    public String getName() {
        return "ghost";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.withRequiredPermissions(getName(), "Move alle User in einen temporären Channel.", PermissionType.ADMINISTRATOR)
                .setEnabledInDms(false);
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        var curChannel = either.getLeft().getUser()
                .getConnectedVoiceChannel(either.getRight());

        if (curChannel.isEmpty()) {
            interUpdater.setContent("Du befindest dich ein keinen Voice-Kanal!").update();
            return;
        }

        var originalIdOpt = MusicBot.DATABASE
                .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                .getValue(curChannel.get().getId());

        originalIdOpt.ifPresentOrElse(originalID -> {
            either.getRight().getVoiceChannelById(originalID).ifPresent(originalCh -> {
                //move users
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                curChannel.get().getConnectedUsers().forEach(user -> futures.add(user.move(originalCh)));

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
                interUpdater.setContent("Der Ghost-Kanal wurde gelöscht.").update();
            });

            MusicBot.DATABASE
                    .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                    .deleteValue(curChannel.get().getId());
            curChannel.get().delete().join();
        }, () -> {
            var channelBuilder = either.getRight().createVoiceChannelBuilder()
                    .setName(curChannel.get().getName() + " \uD83D\uDC7B")
                    .setUserlimit(curChannel.get().getUserLimit().orElse(0))
                    .setCategory(curChannel.get().getCategory().orElse(null));
            channelBuilder.addPermissionOverwrite(either.getRight().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.VIEW_CHANNEL).build());
            curChannel.get().getConnectedUsers().forEach(user -> channelBuilder.addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS, PermissionType.SPEAK).build()));

            var newChannel = channelBuilder.create().join();

            MusicBot.DATABASE
                    .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                    .putValue(newChannel.getId(), curChannel.get().getId());

            //move users
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            curChannel.get().getConnectedUsers().forEach(user -> futures.add(user.move(newChannel)));

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            interUpdater.setContent("Der Ghost-Kanal wurde erstellt.").update();
        });
    }
}
