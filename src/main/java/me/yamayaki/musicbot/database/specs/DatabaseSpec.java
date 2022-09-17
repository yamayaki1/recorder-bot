package me.yamayaki.musicbot.database.specs;

public class DatabaseSpec<K, V> {
    public final Integer prefix;
    public final Class<K> key;
    public final Class<V> value;

    public DatabaseSpec(Integer prefix, Class<K> key, Class<V> value) {
        this.prefix = prefix;
        this.key = key;
        this.value = value;
    }
}
