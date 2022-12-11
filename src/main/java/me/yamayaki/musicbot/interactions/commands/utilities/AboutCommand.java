package me.yamayaki.musicbot.interactions.commands.utilities;

import me.yamayaki.musicbot.Config;
import me.yamayaki.musicbot.interactions.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

public class AboutCommand implements Command {
    @Override
    public String getName() {
        return "about";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Informationen über den Bot")
                .setEnabledInDms(false);
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        var interUpdater = interaction.respondLater(true).join();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Über diesen Bot").setUrl("https://github.com/yamayaki1/recorder-bot")

                .addField("Bot-Version", Config.getVersion(), true)
                .addField("Datenbank-Version", Config.getDatabaseVersion(), true)
                .addField("Discord-Version", Config.getDiscordVersion(), true)

                .addField("\u200b", "\u200b", false)
                .addField("Betriebssystem", Config.getOsInfo(), true)
                .addField("Arbeitsspeicher", Config.getRamInfo(), true);

        interUpdater.addEmbed(embedBuilder).update();
    }
}
