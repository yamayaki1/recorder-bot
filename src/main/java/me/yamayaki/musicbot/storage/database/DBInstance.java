package me.yamayaki.musicbot.storage.database;

import me.yamayaki.musicbot.storage.database.specs.DatabaseSpec;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.TransactionDB;
import org.rocksdb.TransactionDBOptions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBInstance {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final TransactionDB database;

    private final HashMap<DatabaseSpec<?, ?>, DBFamily<?, ?>> databases = new HashMap<>();

    public DBInstance(File file, DatabaseSpec<?, ?>[] specs) {
        if (file.mkdirs() && !file.isDirectory()) {
            throw new RuntimeException("Couldn't create directory: " + file);
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
                        .setCreateMissingColumnFamilies(true);
                TransactionDBOptions transactionOptions = new TransactionDBOptions()
        ) {
            this.database = TransactionDB.open(options, transactionOptions, file.getPath(), familyDescriptors, familyHandles);

            for (DatabaseSpec<?, ?> spec : specs) {
                ColumnFamilyHandle handle = null;
                for (ColumnFamilyHandle familyHandle : familyHandles) {
                    if (Arrays.equals(spec.name.getBytes(StandardCharsets.UTF_8), familyHandle.getName())) {
                        handle = familyHandle;
                        break;
                    }
                }

                this.databases.put(spec, new DBFamily<>(spec, this, handle));
            }
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to open database: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V> DBFamily<K, V> getDatabase(DatabaseSpec<K, V> spec) {
        DBFamily<?, ?> instance = this.databases.get(spec);

        if (instance == null) {
            throw new NullPointerException("No database is registered for spec " + spec);
        }

        return (DBFamily<K, V>) instance;
    }

    public TransactionDB getRocks() {
        return this.database;
    }

    public ReentrantReadWriteLock getLock() {
        return this.lock;
    }

    public void flush() {
        for (DBFamily<?, ?> database : this.databases.values()) {
            database.commit();
        }
    }

    public void close() throws RocksDBException {
        this.flush();

        for (DBFamily<?, ?> database : this.databases.values()) {
            database.close();
        }

        this.database.closeE();
    }
}
