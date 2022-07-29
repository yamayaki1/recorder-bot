package me.jerriidesu.musicbot.commands;

import com.mojang.brigadier.CommandDispatcher;
import org.javacord.api.event.message.MessageCreateEvent;

public interface Command {
    void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher);
}
