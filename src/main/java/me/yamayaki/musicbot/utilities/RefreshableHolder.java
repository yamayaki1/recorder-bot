package me.yamayaki.musicbot.utilities;

import me.yamayaki.musicbot.MusicBot;

import java.util.function.Consumer;

public class RefreshableHolder<T> {
    private final CheckedFunction<T, T> updater;
    private T holdable;

    public RefreshableHolder(CheckedFunction<T, T> updater) {
        this.holdable = null;
        this.updater = updater;
    }

    public T getHoldable() {
        this.update();
        return this.holdable;
    }

    public void setHoldable(T holdable) {
        this.holdable = holdable;
    }

    public void ifSet(Consumer<T> consumer) {
        this.update();
        if (this.holdable != null) {
            consumer.accept(this.holdable);
        }
    }

    public void ifSetOrElse(Consumer<T> consumer, Runnable runnable) {
        this.update();
        if (this.holdable != null) {
            consumer.accept(this.holdable);
        } else {
            runnable.run();
        }
    }

    public void update() {
        if (this.holdable == null) {
            return;
        }

        try {
            this.holdable = this.updater.apply(this.holdable);
        } catch (Exception e) {
            this.holdable = null;
            MusicBot.LOGGER.error(e);
        }
    }
}
