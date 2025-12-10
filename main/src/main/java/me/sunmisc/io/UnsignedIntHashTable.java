package me.sunmisc.io;

import me.sunmisc.io.alloc.Alloc;
import me.sunmisc.io.page.Page;
import me.sunmisc.io.page.SegmentsPage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public final class UnsignedIntHashTable implements Table {
    private static final int DEFAULT_CAPACITY = 128;
    private static final float LOAD_FACTOR = 0.75F;
    private static final int NEED_FREE_SLOTS = 2;

    private final Alloc allocation;
    private final Page table;
    // header
    private final List<Page> down = new ArrayList<>(30); // fixed size
    private int occupied;

    public UnsignedIntHashTable(final Alloc allocation) throws IOException {
        this(allocation, DEFAULT_CAPACITY);
    }

    public UnsignedIntHashTable(final Alloc allocation, final int capacity) throws IOException {
        this(allocation, allocation.alloc(capacity));
    }

    public UnsignedIntHashTable(final Alloc allocation, final Page first) {
        this.allocation = allocation;
        this.down.add(first);
        this.table = new SegmentsPage(this.down);
    }

    @Override
    public void put(final int key, final int value) throws IOException {
        validationKey(key);
        final int n = this.table.length();
        final int th = (int) (n * LOAD_FACTOR);
        if ((this.occupied + NEED_FREE_SLOTS) >= th) {
            // rehash
            this.down.add(
                    this.allocation.alloc(
                            this.down.getLast().length() << 1
                    )
            );
            // todo: FIFO to file
            final List<Map.Entry<Integer, Integer>> list = new ArrayList<>();
            for (int i = 0; i < n; i += 2) {
                final int k = this.table.readInt(i);
                if (k < 0) {
                    continue;
                }
                final int v = this.table.readInt(i + 1);
                this.insert(i, -1, -1); // mark as deleted
                list.add(Map.entry(k, v));
            }
            for (final Map.Entry<Integer, Integer> e : list) {
                final int k = e.getKey();
                final int v = e.getValue();
                this.insert(-(this.probe(k) + 1), k, v);
            }
        }
        int idx = this.probe(key);
        if (idx < 0) {
            idx = -(idx + 1);
            this.insert(idx, key, value);
            this.occupied += 2;
        } else {
            // replace
            this.table.writeInt(idx + 1, value);
        }
    }

    @Override
    public Optional<Integer> get(final int key) throws IOException {
        validationKey(key);
        final int index = this.probe(key);
        return index < 0
                ? Optional.empty()
                : Optional.of(this.table.readInt(index + 1));
    }

    @Override
    public int size() {
        return this.occupied >>> 1;
    }

    @Override
    public long offset() {
        return this.table.offset();
    }

    private void insert(final int index, final int key, final int value) throws IOException {
        final long packed = ((long) key << 32) | (value & 0xFFFFFFFFL);
        this.table.writeLong(index, packed);
    }

    private Map.Entry<Integer, Integer> unpacked(final long word) {
        final int key = (int) (word >>> 32);
        final int value = (int) word;
        return Map.entry(key, value);
    }

    private static int hash(final int h, final int length) {
        // Multiply by -254 to use the hash LSB and to ensure index is even
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    private static int nextKeyIndex(final int i, final int len) {
        return (i + 2 < len ? i + 2 : 0);
    }

    private static void validationKey(final int key) {
        if (key < 0) {
            throw new IllegalArgumentException(
                    "This hash table implementation does not support negative keys"
            );
        }
    }

    private int probe(final int pk) throws IOException {
        final int n = this.table.length();
        int idx = hash(pk, n);
        while (true) {
            final int ek = this.table.readInt(idx);
            if (ek == -1) {
                return -idx - 1;
            } else if (ek == pk) {
                return idx;
            }
            idx = nextKeyIndex(idx, n);
        }
    }

    @Override
    public String toString() {
        return String.format("""
                        size = %s, capacity = %s
                        table:
                        %s
                        """,
                this.size(),
                this.table.length(),
                this.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ", "[", "]"))
        );
    }

    @Override
    public Iterator<Map.Entry<Integer, Integer>> iterator() {
        return StreamSupport.stream(this.spliterator(), false).iterator();
    }

    @Override
    public Spliterator<Map.Entry<Integer, Integer>> spliterator() {
        return IntStream.range(0, this.table.length() >>> 1)
                .map(i -> i << 1)
                .mapToObj(i -> {
                    try {
                        return this.unpacked(this.table.readLong(i));
                    } catch (final IOException ex) {
                        throw new RuntimeException(String.format("failed to read slot %s", i), ex);
                    }
                })
                .filter(e -> e.getKey() >= 0)
                .spliterator();
    }
}
