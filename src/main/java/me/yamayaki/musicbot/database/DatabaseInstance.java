package me.yamayaki.musicbot.database;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.database.serializers.Serializer;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteOptions;

import java.io.IOException;
import java.util.Optional;

public class DatabaseInstance<K, V> {
    private final RocksManager rocksManager;

    private final DatabaseSpec<K, V> spec;

    private final Serializer<K> keySerializer;
    private final Serializer<V> valSerializer;

    public DatabaseInstance(DatabaseSpec<K, V> spec, RocksManager rocksManager) {
        this.rocksManager = rocksManager;

        this.spec = spec;

        this.keySerializer = DefaultSerializers.getSerializer(spec.key);
        this.valSerializer = DefaultSerializers.getSerializer(spec.value);
    }

    public void putValue(K key, V value) {
        this.rocksManager.getLock()
                .writeLock()
                .lock();

        try (WriteOptions writeOptions = new WriteOptions()) {
            this.rocksManager.getRocks().put(
                    writeOptions,
                    this.keySerializer.serialize(this.spec.prefix, key),
                    this.valSerializer.serialize(value)
            );
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error writing value: ", e);
        } finally {
            this.rocksManager.getLock()
                    .writeLock()
                    .unlock();
        }
    }

    public Optional<V> getValue(K key) {
        this.rocksManager.getLock()
                .writeLock()
                .lock();

        try (ReadOptions readOptions = new ReadOptions()) {
            byte[] data = this.rocksManager.getRocks().get(
                    readOptions,
                    this.keySerializer.serialize(this.spec.prefix, key)
            );

            return data != null ? Optional.of(this.valSerializer.deserialize(data)) : Optional.empty();
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error reading value: ", e);
            return Optional.empty();
        } finally {
            this.rocksManager.getLock()
                    .writeLock()
                    .unlock();
        }
    }

    public void deleteValue(K key) {
        this.rocksManager.getLock()
                .writeLock()
                .lock();

        try (WriteOptions writeOptions = new WriteOptions()) {
            this.rocksManager.getRocks().delete(
                    writeOptions,
                    this.keySerializer.serialize(this.spec.prefix, key)
            );
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error deleting value: ", e);
        } finally {
            this.rocksManager.getLock()
                    .writeLock()
                    .unlock();
        }
    }
}
