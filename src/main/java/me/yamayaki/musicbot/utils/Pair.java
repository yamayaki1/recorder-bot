package me.yamayaki.musicbot.utils;

public class Pair<L, R> {
    private final Single<L> left;
    private final Single<R> right;

    public Pair(L left, R right) {
        this.left = new Single<>(left);
        this.right = new Single<>(right);
    }

    public L getLeft() {
        return this.left.defaultValue();
    }

    public R getRight() {
        return this.right.defaultValue();
    }

    private record Single<T>(T value) {
        public T defaultValue() {
            return this.value;
        }
    }
}
