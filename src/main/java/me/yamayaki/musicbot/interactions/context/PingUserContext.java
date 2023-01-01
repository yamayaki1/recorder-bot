package me.yamayaki.musicbot.interactions.context;

import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.UserContextMenu;
import org.javacord.api.interaction.UserContextMenuBuilder;
import org.javacord.api.interaction.UserContextMenuInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

public class PingUserContext implements ApplicationInteraction {
    @Override
    public String getName() {
        return "user_ping";
    }

    @Override
    public boolean isExperimental() {
        return false;
    }

    @Override
    public UserContextMenuBuilder register(DiscordApi api) {
        return UserContextMenu.with(getName())
                .addNameLocalization(DiscordLocale.GERMAN, "Benuter pingen")
                .addNameLocalization(DiscordLocale.ENGLISH_US, "Ping User")
                .setEnabledInDms(false);
    }

    @Override
    public void executeContext(UserContextMenuInteraction interaction, InteractionOriginalResponseUpdater response) {
        interaction.getTarget().openPrivateChannel().thenAccept(privateChannel -> {
            StringBuilder builder = new StringBuilder("Du wurdest von ");
            builder.append(interaction.getUser().getDiscriminatedName());

            interaction.getServer().ifPresent(server -> {
                builder.append(" in ");
                builder.append(server.getName());
            });

            builder.append(" angepingt!");

            privateChannel.sendMessage(builder.toString()).join();
        }).join();

        response.setContent("Der Benutzer wurde erfolgreich angepingt!");
    }
}
