package me.jerriidesu.musicbot.utils;

import com.vdurmont.emoji.EmojiParser;
import me.jerriidesu.musicbot.MusicBot;
import org.javacord.api.entity.message.Message;

public class Reactions {

    public static String DISCARD = ":fire_extinguisher:";

    public static void addTrashReaction(Message message) {
        message.addReaction(EmojiParser.parseToUnicode(DISCARD)).join();
    }

    public static void addSuccessfullReaction(Message message) {
        message.addReaction(EmojiParser.parseToUnicode(MusicBot.getConfig().get().getReactions().getSuccess()));
    }

    public static void addFailureReaction(Message message) {
        message.addReaction(EmojiParser.parseToUnicode(MusicBot.getConfig().get().getReactions().getFailure()));
    }

    public static void addRefuseReaction(Message message) {
        message.addReaction(EmojiParser.parseToUnicode(MusicBot.getConfig().get().getReactions().getRefuse()));
    }
}
