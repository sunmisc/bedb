package me.sunmisc.io;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import me.sunmisc.io.alloc.Alloc;
import me.sunmisc.io.alloc.AllocHeapTable;
import me.sunmisc.io.page.Page;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

public final class FreeSpaceMap implements Free {
    private static final int PAGE_SIZE = 32;

    private final AtomicInteger tail = new AtomicInteger();
    private final Alloc alloc;
    private final LoadingCache<Integer, Page> striped;

    public FreeSpaceMap(Alloc alloc) {
        this.alloc = alloc;
        this.striped =  Caffeine
                .newBuilder()
                .maximumSize(100)
                .expireAfterAccess(Duration.ofMinutes(5))
                .build(off -> alloc.alloc(PAGE_SIZE));
    }

    public static void main(String[] args) throws IOException {
        FreeSpaceMap spaceMap = new FreeSpaceMap(new AllocHeapTable());
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
            final int v = tail.get();
            if (v <= 0) {
                return Optional.empty();
            } else if (tail.weakCompareAndSetVolatile(v, v - 1)) {
                int k = Math.floorDiv(v - 1, PAGE_SIZE);
                int q = (v - 1) % PAGE_SIZE;
                return Optional.of(
                        new Location.LongLocation(
                                striped.get(k).readInt(q)
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
        int v = tail.getAndAdd(count);
        for (Location loc : locations) {
            int k = Math.floorDiv(v, PAGE_SIZE);
            int q = v % PAGE_SIZE;
            striped.get(k).writeInt(q, (int) loc.offset());
            v++;
        }
    }

    @Override
    public int size() {
        return tail.get();
    }
}
