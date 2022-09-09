package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

public class HelpCommand implements Command {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Zeige alle vorhandene Befehle")
                .setEnabledInDms(false);
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Befehlsübersicht")
                .addField("Bot verbinden", "/connect")
                .addField("Bot trennen", "/disconnect")
                .addField("Musik abspielen", "/play <spotify*|youtube|twitch|soundcloud|search>")
                .addField("Music pausieren*", "/pause")
                .addField("Music fortsetzen*", "/resume")
                .addField("Music überspringen*", "/skip [track-id]")
                .addField("Lautstärke ändern", "/volume <0-120>")
                .addField("Playlist anzeigen*", "/playlist")
                .addField("Playlist wiederholen*", "/loop")
                .addField("Playlist leeren", "/clear")
                .addField("*", "Indev-Funktion");

        interUpdater.addEmbed(embedBuilder).update();
    }
}
