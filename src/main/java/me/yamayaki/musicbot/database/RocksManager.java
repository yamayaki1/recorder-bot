package me.yamayaki.musicbot.database;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RocksManager {
    private static boolean shownStats = false;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final RocksDB database;

    private final HashMap<DatabaseSpec<?, ?>, DatabaseInstance<?, ?>> databases = new HashMap<>();

    public RocksManager(File file, DatabaseSpec<?, ?>[] specs) {
        if (file.mkdirs() && !file.isDirectory()) {
            throw new RuntimeException("Couldn't create directory: " + file);
        }

        if (!shownStats) {
            MusicBot.LOGGER.info("using rocksdb ({}) as database backend ...", RocksDB.rocksdbVersion());
            shownStats = true;
        }

        final List<ColumnFamilyDescriptor> familyDescriptors = new ArrayList<>();
        final List<ColumnFamilyHandle> familyHandles = new ArrayList<>();

        familyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY));

        for (DatabaseSpec<?, ?> spec : specs) {
            familyDescriptors.add(new ColumnFamilyDescriptor(spec.name.getBytes(StandardCharsets.UTF_8)));
        }

        try (
                DBOptions options = new DBOptions()
                        .setCreateIfMissing(true)
                        .setKeepLogFileNum(2)
                        .setCreateMissingColumnFamilies(true)
        ) {
            this.database = RocksDB.open(options, file.getPath(), familyDescriptors, familyHandles);

            for (DatabaseSpec<?, ?> spec : specs) {
                ColumnFamilyHandle handle = null;
                for (ColumnFamilyHandle familyHandle : familyHandles) {
                    if (Arrays.equals(spec.name.getBytes(StandardCharsets.UTF_8), familyHandle.getName())) {
                        handle = familyHandle;
                        break;
                    }
                }

                this.databases.put(spec, new DatabaseInstance<>(spec, this, handle));
            }
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to open database: ", e);
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
        for (DatabaseInstance<?, ?> database : this.databases.values()) {
            database.close();
        }

        this.database.closeE();
    }
}
