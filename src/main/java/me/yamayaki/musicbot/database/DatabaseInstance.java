package me.yamayaki.musicbot.database;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.database.serializers.Serializer;
import org.rocksdb.CompactionStyle;
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
    private static boolean shownStats = false;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final RocksDB database;

    private final Serializer<K> keySerializer;
    private final Serializer<V> valSerializer;

    public DatabaseInstance(File file, DatabaseSpec<K, V> spec) {
        if (file.mkdirs() && !file.isDirectory()) {
            throw new RuntimeException("Couldn't create directory: " + file);
        }

        if (!shownStats) {
            MusicBot.LOGGER.info("using rocksdb ({}) as caching backend ...", RocksDB.rocksdbVersion());
            shownStats = true;
        }

        this.keySerializer = DefaultSerializers.getSerializer(spec.key);
        this.valSerializer = DefaultSerializers.getSerializer(spec.value);

        try (
                Options options = new Options()
                        .setCreateIfMissing(true)
                        .setKeepLogFileNum(2)
                        .setCompressionType(CompressionType.SNAPPY_COMPRESSION)
                        .setCompactionStyle(CompactionStyle.FIFO)
        ) {
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
            return Optional.empty();
        } finally {
            this.lock.readLock()
                    .unlock();
        }
    }

    public void deleteValue(K key) {
        this.lock.writeLock()
                .lock();

        try (WriteOptions writeOptions = new WriteOptions()) {
            this.database.delete(
                    writeOptions,
                    this.keySerializer.serialize(key)
            );
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error deleting value: ", e);
        } finally {
            this.lock.writeLock()
                    .unlock();
        }
    }

    public void close() throws RocksDBException {
        this.database.closeE();
    }
}
