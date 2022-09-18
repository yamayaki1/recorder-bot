package me.yamayaki.musicbot.database;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.database.serializers.Serializer;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteOptions;

import java.io.IOException;
import java.util.Optional;

public class DatabaseInstance<K, V> {
    private final RocksManager rocksManager;
    private final ColumnFamilyHandle columnHandle;

    private final Serializer<K> keySerializer;
    private final Serializer<V> valSerializer;

    public DatabaseInstance(DatabaseSpec<K, V> spec, RocksManager rocksManager, ColumnFamilyHandle columnHandle) {
        this.rocksManager = rocksManager;

        if(columnHandle == null) {
            throw new RuntimeException("cant open instance, handle is null");
        }

        this.columnHandle = columnHandle;

        this.keySerializer = DefaultSerializers.getSerializer(spec.key);
        this.valSerializer = DefaultSerializers.getSerializer(spec.value);
    }

    public void putValue(K key, V value) {
        this.rocksManager.getLock()
                .writeLock()
                .lock();

        try (WriteOptions writeOptions = new WriteOptions()) {
            this.rocksManager.getRocks().put(
                    this.columnHandle,
                    writeOptions,
                    this.keySerializer.serialize(key),
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
                    this.columnHandle,
                    readOptions,
                    this.keySerializer.serialize(key)
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
                    this.columnHandle,
                    writeOptions,
                    this.keySerializer.serialize(key)
            );
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error deleting value: ", e);
        } finally {
            this.rocksManager.getLock()
                    .writeLock()
                    .unlock();
        }
    }

    public void close() {
        this.columnHandle.close();
    }
}
