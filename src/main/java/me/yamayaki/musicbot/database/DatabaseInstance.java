package me.yamayaki.musicbot.database;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.database.serializers.Serializer;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteOptions;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseInstance<K, V> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final RocksDB database;

    private final Serializer<K> keySerializer;
    private final Serializer<V> valSerializer;

    public DatabaseInstance(File file, DatabaseSpec<K, V> spec) {
        if (file.mkdirs() && !file.isDirectory()) {
            throw new RuntimeException("Couldn't create directory: " + file);
        }

        this.keySerializer = DefaultSerializers.getSerializer(spec.key);
        this.valSerializer = DefaultSerializers.getSerializer(spec.value);

        try (Options options = new Options().setCreateIfMissing(true).setLogFileTimeToRoll(60).setCompressionType(CompressionType.SNAPPY_COMPRESSION)) {
            this.database = RocksDB.open(options, file.getPath());
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to open database: ", e);
        }
    }

    public void putValue(K key, V value) {
        this.lock.writeLock()
                .lock();

        try (WriteOptions writeOptions = new WriteOptions()) {
            this.database.put(
                    writeOptions,
                    this.keySerializer.serialize(key),
                    this.valSerializer.serialize(value)
            );
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error writing value: ", e);
        } finally {
            this.lock.writeLock()
                    .unlock();
        }
    }

    public Optional<V> getValue(K key) {
        this.lock.readLock()
                .lock();

        try (ReadOptions readOptions = new ReadOptions()) {
            byte[] data = this.database.get(
                    readOptions,
                    this.keySerializer.serialize(key)
            );

            return data != null ? Optional.of(this.valSerializer.deserialize(data)) : Optional.empty();
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error reading value: ", e);
        } finally {
            this.lock.readLock()
                    .unlock();
        }

        return Optional.empty();
    }

    public void close() throws RocksDBException {
        this.database.closeE();
    }
}
