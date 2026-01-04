package me.sunmisc.io;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import me.sunmisc.io.alloc.Alloc;
import me.sunmisc.io.alloc.AllocFileDefault;
import me.sunmisc.io.alloc.AllocIntPage;
import me.sunmisc.io.page.AtomicPage;
import me.sunmisc.io.page.LockPage;
import me.sunmisc.io.page.Page;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class FreeSpaceMap implements Free {
    private static final int PAGE_SIZE = 32;

    private final Alloc alloc;
    private final LoadingCache<Long, Page> striped;
    private final AtomicPage tail;

    public FreeSpaceMap(Alloc alloc) throws IOException {
        this.alloc = alloc;
        this.tail = new LockPage(alloc.alloc(2));
        this.striped = Caffeine
                .newBuilder()
                .maximumSize(100)
                .expireAfterAccess(Duration.ofMinutes(5))
                .build(off -> alloc.take(new Location.LongLocation((off * (PAGE_SIZE * 2)) + 8)));
    }

    public static void main(String[] args) throws IOException {
        FreeSpaceMap spaceMap = new FreeSpaceMap(
                new AllocIntPage(new AllocFileDefault(new File("kek.bin"))));
        for (long off = 0; off < 1024; off += 8) {
            spaceMap.add(List.of(new Location.LongLocation(off)));
        }
        for (long off = 0; off < 1024; off += 8) {
            System.out.println(spaceMap.poll(0));
        }
    }

    @Override
    public Optional<Location> poll(int require) throws IOException {
        for (;;) {
            final long v = tail.readLong(0);
            if (v <= 0) {
                return Optional.empty();
            } else if (tail.caeLong(0, v, v - 1) == v) {
                long k = Math.floorDiv(v - 1, PAGE_SIZE);
                int q = Math.toIntExact((v - 1) % PAGE_SIZE);
                return Optional.of(
                        new Location.LongLocation(
                                striped.get(k).readLong(q)
                        )
                );
            }
        }
    }

    @Override
    public void add(Iterable<Location> locations) throws IOException {
        final int count = Math.toIntExact(
                StreamSupport
                        .stream(locations.spliterator(), false)
                        .count()
        );
        long v = Math.max(0, tail.computeLong(0, q -> Math.max(0, q) + count));
        for (Location loc : locations) {
            long k = Math.floorDiv(v, PAGE_SIZE);
            int q = Math.toIntExact(v % PAGE_SIZE);
            striped.get(k, integer -> {
                try {
                    return alloc.alloc(PAGE_SIZE * 2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).writeLong(q, (int) loc.offset());
            v++;
        }
    }

    @Override
    public int size() {
        try {
            return Math.toIntExact(tail.readLong(0));
        } catch (IOException e) {
            return  0;
        }
    }
}
