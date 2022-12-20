package me.yamayaki.musicbot.storage.database;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.storage.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.storage.database.serializers.Serializer;
import me.yamayaki.musicbot.storage.database.specs.DatabaseSpec;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDBException;
import org.rocksdb.Status;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;

import java.io.IOException;
import java.util.Optional;

public class DBFamily<K, V> {
    private final DBInstance dbInstance;

    private final ColumnFamilyHandle columnHandle;

    private final Serializer<K> keySerializer;
    private final Serializer<V> valSerializer;

    private Transaction dbTransaction;

    public DBFamily(DatabaseSpec<K, V> spec, DBInstance DBInstance, ColumnFamilyHandle columnHandle) {
        this.dbInstance = DBInstance;

        if (columnHandle == null) {
            throw new RuntimeException("cant open instance, handle is null");
        }

        this.columnHandle = columnHandle;

        this.keySerializer = DefaultSerializers.getSerializer(spec.key);
        this.valSerializer = DefaultSerializers.getSerializer(spec.value);
    }

    public void putValue(K key, V value) {
        this.ensureTransaction();

        try {
            this.dbTransaction.put(
                    this.columnHandle,
                    this.keySerializer.serialize(key),
                    this.valSerializer.serialize(value)
            );
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error while writing to transaction: ", e);
        }
    }

    public Optional<V> getValue(K key) {
        this.ensureTransaction();

        try (ReadOptions readOptions = new ReadOptions()) {
            byte[] data = this.dbTransaction.get(
                    this.columnHandle,
                    readOptions,
                    this.keySerializer.serialize(key)
            );

            return data != null ? Optional.of(this.valSerializer.deserialize(data)) : Optional.empty();
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error while reading from transaction: ", e);
            return Optional.empty();
        }
    }

    public void deleteValue(K key) {
        this.ensureTransaction();

        try {
            this.dbTransaction.delete(
                    this.columnHandle,
                    this.keySerializer.serialize(key)
            );
        } catch (RocksDBException | IOException e) {
            MusicBot.LOGGER.error("Error while deleting value from transaction: ", e);
        }
    }

    private void ensureTransaction() {
        try (WriteOptions writeOptions = new WriteOptions()) {
            if (this.dbTransaction == null) {
                this.dbTransaction = this.dbInstance.getRocks()
                        .beginTransaction(writeOptions);
                return;
            }

            if (this.dbTransaction.getState() == Transaction.TransactionState.COMMITTED) {
                this.dbTransaction = this.dbInstance.getRocks()
                        .beginTransaction(writeOptions, this.dbTransaction);
            }
        }
    }

    public void commit() {
        this.ensureTransaction();
        this.dbInstance.getLock()
                .writeLock().lock();

        try {
            this.dbTransaction.commit();
        } catch (RocksDBException e) {
            if (e.getStatus().getCode() == Status.Code.Expired) {
                MusicBot.LOGGER.error("Trying to commit expired transaction, rebuilding: ", e);

                try (WriteOptions writeOptions = new WriteOptions()) {
                    var writeBatch = this.dbTransaction.getCommitTimeWriteBatch();
                    this.dbTransaction = this.dbInstance.getRocks()
                            .beginTransaction(writeOptions);
                    this.dbTransaction.rebuildFromWriteBatch(writeBatch);
                } catch (RocksDBException ex) {
                    MusicBot.LOGGER.error("Error while restoring from write-batch, aborting: ", e);
                }

                return;
            }

            MusicBot.LOGGER.error("Error while committing transaction: ", e);
        } finally {
            this.dbInstance.getLock()
                    .writeLock().unlock();
        }
    }

    public void close() {
        this.columnHandle.close();
    }
}
