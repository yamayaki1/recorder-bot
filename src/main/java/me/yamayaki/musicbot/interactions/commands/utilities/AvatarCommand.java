package me.yamayaki.musicbot.interactions.commands.utilities;

import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

import java.awt.*;

public class AvatarCommand implements Command {
    @Override
    public String getName() {
        return "avatar";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Zeigt den Avatar eines Benutzers")
                .setEnabledInDms(false)
                .addOption(SlashCommandOption.createUserOption("user", "Benutzer", true));
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(false).join();

        either.getLeft().getArgumentUserValueByName("user").ifPresentOrElse(user -> {
            EmbedBuilder embed = new EmbedBuilder().setColor(Color.GREEN)
                    .setAuthor(user)
                    .setImage(user.getAvatar());

            interUpdater.addEmbed(embed).update();
        }, () -> interUpdater.setContent("Ungültiger Benutzer.").update());
    }
}
