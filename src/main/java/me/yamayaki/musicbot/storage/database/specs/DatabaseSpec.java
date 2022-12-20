package me.yamayaki.musicbot.storage.database.specs;

public class DatabaseSpec<K, V> {
    public final String name;
    public final Class<K> key;
    public final Class<V> value;

    public DatabaseSpec(String name, Class<K> key, Class<V> value) {
        this.name = name;
        this.key = key;
        this.value = value;
    }
}
