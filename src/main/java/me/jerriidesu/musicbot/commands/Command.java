package me.jerriidesu.musicbot.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.jerriidesu.musicbot.utils.Either;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public interface Command {
    void registerBrigadier(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher);
}
