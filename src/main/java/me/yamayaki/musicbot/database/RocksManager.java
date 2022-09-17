package me.yamayaki.musicbot.database;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RocksManager {
    private static boolean shownStats = false;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final RocksDB database;

    private final Reference2ReferenceMap<DatabaseSpec<?, ?>, DatabaseInstance<?, ?>> databases = new Reference2ReferenceOpenHashMap<>();

    public RocksManager(File file, DatabaseSpec<?, ?>[] specs) {
        if (file.mkdirs() && !file.isDirectory()) {
            throw new RuntimeException("Couldn't create directory: " + file);
        }

        if (!shownStats) {
            MusicBot.LOGGER.info("using rocksdb ({}) as caching backend ...", RocksDB.rocksdbVersion());
            shownStats = true;
        }

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

        for (DatabaseSpec<?, ?> spec : specs) {
            this.databases.put(spec, new DatabaseInstance<>(spec, this));
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V> DatabaseInstance<K, V> getDatabase(DatabaseSpec<K, V> spec) {
        DatabaseInstance<?, ?> instance = this.databases.get(spec);

        if (instance == null) {
            throw new NullPointerException("No database is registered for spec " + spec);
        }

        return (DatabaseInstance<K, V>) instance;
    }

    public RocksDB getRocks() {
        return this.database;
    }

    public ReentrantReadWriteLock getLock() {
        return this.lock;
    }

    public void close() throws RocksDBException {
        this.database.closeE();
    }
}
