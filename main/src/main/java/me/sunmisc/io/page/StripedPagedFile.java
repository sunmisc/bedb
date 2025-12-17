package me.sunmisc.io.page;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Weigher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public final class StripedPagedFile implements Page, AutoCloseable {
    private static final int PAGE_SIZE = 4096;
    private final RandomAccessFile raf;
    private final long offset;
    private final int size;
    private final LoadingCache<Integer, Page> cache;

    public StripedPagedFile(final File origin, final long offset, final int size) throws IOException {
        this.raf = new RandomAccessFile(origin, "rw");
        this.cache = Caffeine
                .newBuilder()
                .weigher((Weigher<Integer, Page>) (key, value) -> value.length())
                .maximumWeight(PAGE_SIZE * 32)
                //.expireAfterAccess(Duration.ofMinutes(5))
                .build(off -> {
                    final int len = off * PAGE_SIZE;
                    final long start = offset + len;
                    return new BufferPage(
                            start,
                            raf.getChannel().map(
                                    FileChannel.MapMode.READ_WRITE,
                                    start,
                                    Math.min(PAGE_SIZE, size - len)
                            ).order(ByteOrder.BIG_ENDIAN)
                    );
        });
        this.offset = offset;
        this.size = size;
    }

    private Page lookup(int index) {
        return cache.get(index);
    }

    @Override
    public void writeInt(int index, int value) throws IOException {
        final int page = Math.floorDiv(index, PAGE_SIZE);
        lookup(page).writeInt(index & (PAGE_SIZE - 1), value);
    }

    @Override
    public int readInt(int index) throws IOException {
        final int page = Math.floorDiv(index, PAGE_SIZE);
        return lookup(page).readInt(index & (PAGE_SIZE - 1));
    }

    @Override
    public void writeLong(int index, long value) throws IOException {
        final int page = Math.floorDiv(index, PAGE_SIZE);
        lookup(page).writeLong(index & (PAGE_SIZE - 1), value);
    }

    @Override
    public long readLong(int index) throws IOException {
        final int page = Math.floorDiv(index, PAGE_SIZE);
        return lookup(page).readLong(index & (PAGE_SIZE - 1));
    }

    @Override
    public long offset() {
        return this.offset;
    }

    @Override
    public int length() {
        return this.size;
    }

    @Override
    public void close() throws Exception {
        this.raf.close();
    }
}
