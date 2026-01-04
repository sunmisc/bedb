package me.sunmisc.io.alloc;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import me.sunmisc.io.Location;
import me.sunmisc.io.page.Page;

import java.io.IOException;
import java.time.Duration;

public final class AllocCachedPage implements Alloc {
    private final Alloc origin;
    private final LoadingCache<Location, Page> cache;

    public AllocCachedPage(final Alloc origin) {
        this.origin = origin;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .weakValues()
                .build(origin::take);
    }

    @Override
    public Page alloc(final int size) throws IOException {
        final Page alloc = origin.alloc(size);
        cache.put(alloc, alloc);
        return alloc;
    }

    @Override
    public Page take(final Location location) {
        return cache.get(location);
    }

    @Override
    public void free(final Location location) throws IOException {
        origin.free(location);
        cache.invalidate(location);
    }
}
