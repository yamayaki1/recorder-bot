package me.yamayaki.musicbot.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public interface Command {
    void register(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher);
}
