package me.yamayaki.musicbot.interactions;

import me.yamayaki.musicbot.Config;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.commands.channels.GhostCommand;
import me.yamayaki.musicbot.interactions.commands.channels.PlayerChannelCommand;
import me.yamayaki.musicbot.interactions.commands.music.ClearCommand;
import me.yamayaki.musicbot.interactions.commands.music.PlayCommand;
import me.yamayaki.musicbot.interactions.commands.music.PlayerCommand;
import me.yamayaki.musicbot.interactions.commands.music.PlaylistCommand;
import me.yamayaki.musicbot.interactions.commands.music.SkipCommand;
import me.yamayaki.musicbot.interactions.commands.utilities.AboutCommand;
import me.yamayaki.musicbot.interactions.commands.utilities.PingCommand;
import me.yamayaki.musicbot.interactions.context.PingUserContext;
import me.yamayaki.musicbot.utilities.Threads;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.ApplicationCommandEvent;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.interaction.UserContextMenuCommandEvent;
import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.interaction.UserContextMenuCommandListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class InteractionListener implements SlashCommandCreateListener, ButtonClickListener, UserContextMenuCommandListener {
    private final HashMap<String, ApplicationInteraction> interactions = new HashMap<>();

    public InteractionListener(DiscordApi discordApi) {
        registerInteractions(discordApi,
                new GhostCommand(),
                new PlayerChannelCommand(),

                new ClearCommand(),
                new PlayCommand(),
                new PlayerCommand(),
                new PlaylistCommand(),
                new SkipCommand(),

                new AboutCommand(),
                new PingCommand(),

                new PingUserContext()
        );
    }

    private void registerInteractions(DiscordApi discordApi, ApplicationInteraction... commands) {
        MusicBot.LOGGER.info("registering {} interactions, this may take a while ...", Arrays.stream(commands).count());
        final Set<ApplicationCommandBuilder<?, ?, ?>> builderSet = new HashSet<>();

        for (ApplicationInteraction clazz : commands) {
            if(!clazz.isExperimental() || (clazz.isExperimental() && Config.isDevBuild())) {
                this.interactions.put(clazz.getName(), clazz);
                builderSet.add(clazz.register(discordApi));
            }
        }

        discordApi.bulkOverwriteGlobalApplicationCommands(builderSet).join();
    }

    private void runInteraction(ApplicationCommandEvent interactionEvent) {
        interactionEvent.getInteraction().respondLater(true).thenAcceptAsync(updater -> {
            printInteractionUsed(interactionEvent.getInteraction());

            if (interactionEvent instanceof SlashCommandCreateEvent slashEvent) {
                var slashCommand = slashEvent.getSlashCommandInteraction();

                if (this.interactions.containsKey(slashCommand.getCommandName())) {
                    this.interactions.get(slashCommand.getCommandName()).executeCommand(slashCommand, updater);
                    return;
                }
            }

            if (interactionEvent instanceof UserContextMenuCommandEvent contextEvent) {
                var contextMenu = contextEvent.getUserContextMenuInteraction();

                if (this.interactions.containsKey(contextMenu.getCommandName())) {
                    this.interactions.get(contextMenu.getCommandName()).executeContext(contextMenu, updater);
                    return;
                }
            }

            updater.setContent("Unbekannte Interaktion!").update();
        }, Threads.mainWorker()).orTimeout(120L, TimeUnit.SECONDS).exceptionally(throwable -> {
            interactionEvent.getInteraction().createFollowupMessageBuilder()
                    .setContent("Beim Ausführen des Befehls ist ein Fehler aufgetreten:\n" + throwable.getMessage())
                    .send();

            MusicBot.LOGGER.fatal(throwable);
            return null;
        });
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        this.runInteraction(event);
    }

    @Override
    public void onUserContextMenuCommand(UserContextMenuCommandEvent event) {
        this.runInteraction(event);
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        CompletableFuture.supplyAsync(() -> {
            event.getButtonInteraction().getServer().ifPresent(server -> {
                MusicBot.instance().getAudioManager(server)
                        .getPlayerControl()
                        .onButtonClick(event);
            });

            return null;
        }, Threads.mainWorker()).orTimeout(60L, TimeUnit.SECONDS).exceptionally(throwable -> {
            MusicBot.LOGGER.error(throwable);
            throwable.printStackTrace();
            return 0;
        });
    }

    private void printInteractionUsed(Interaction interaction) {
        StringBuilder builder = new StringBuilder(interaction.getUser().getDiscriminatedName() + " using '/");

        interaction.asSlashCommandInteraction().ifPresent(action-> {
            builder.append(action.getCommandName());
            for (SlashCommandInteractionOption argument : action.getArguments()) {
                builder.append(" ").append(argument.getName()).append(":").append(argument.getStringRepresentationValue().orElse(""));
            }
            builder.append("'");
        });

        interaction.asUserContextMenuInteraction().ifPresent(action-> {
            builder.append(action.getCommandName()).append("'");;
            builder.append(" on ").append(action.getTarget().getDiscriminatedName());
        });

        MusicBot.LOGGER.info(builder);
    }
}
