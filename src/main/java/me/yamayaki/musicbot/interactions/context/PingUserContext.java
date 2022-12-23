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
        return true;
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
            privateChannel.sendMessage("Du wurdest von " + interaction.getUser().getName() + " gepingt!").join();
        }).join();
    }
}
