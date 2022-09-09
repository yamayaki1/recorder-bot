package me.yamayaki.musicbot.database;

public class DatabaseSpec<K, V> {
    protected Class<K> key;
    protected Class<V> value;

    public DatabaseSpec(Class<K> key, Class<V> value) {
        this.key = key;
        this.value = value;
    }
}
