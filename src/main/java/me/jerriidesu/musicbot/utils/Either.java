package me.jerriidesu.musicbot.utils;

@SuppressWarnings("")
public class Either<L, R> {
    private final L left;
    private final R right;

    public Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return this.left;
    }

    public R getRight() {
        return this.right;
    }
}
