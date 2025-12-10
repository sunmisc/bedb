package me.sunmisc.io;

import me.sunmisc.calculus.Cursor;
import me.sunmisc.io.alloc.Alloc;
import me.sunmisc.io.page.Page;
import me.sunmisc.io.page.SegmentsPage;

import java.io.IOException;
import java.util.*;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class IntVector implements Vector {
    private static final int SPINS = 1 << 5;
    private static final int DEFAULT_CAPACITY = 16;
    // share
    private final List<Page> down = new ArrayList<>(30);
    private final Alloc allocation;
    private final Page elements;
    private final AtomicInteger size = new AtomicInteger();
    private final StampedLock lock = new StampedLock();

    public IntVector(final Alloc allocation) throws IOException {
        this(allocation, allocation.alloc(DEFAULT_CAPACITY));
    }

    public IntVector(final Alloc allocation, final Page first) {
        this.allocation = allocation;
        this.down.add(first);
        this.elements = new SegmentsPage(this.down);
    }

    @Override
    public void add(final int value) throws IOException {
        final long stamp = this.lock.writeLock();
        try {
            final int index = this.size.getPlain();
            final int n = index + 1;
            if (this.elements.length() <= index) {
                this.down.add(
                        this.allocation.alloc(
                                this.down.getLast().length() << 1
                        )
                );
            }
            this.elements.writeInt(index, value);
            this.size.set(n);
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    @Override
    public int get(final int index) throws IOException {
        long stamp = 0;
        try {
            for (int spin = 0; ; ++spin) {
                stamp = spin < SPINS
                        ? this.lock.tryOptimisticRead()
                        : this.lock.readLock();
                if (stamp == 0L) {
                    Thread.onSpinWait();
                    continue;
                }
                final int n = this.size.get();
                final int val = index < n ? this.elements.readInt(index) : /* invalid */ 0;
                // loadFence
                if (this.lock.validate(stamp)) {
                    Objects.checkIndex(index, n);
                    return val;
                }
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) { // or spin >= SPINS
                this.lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public void remove(final int index) throws IOException {
        final long stamp = this.lock.writeLock();
        try {
            final int n = this.size.getPlain();
            Objects.checkIndex(index, n);
            final int newSize = this.size.getPlain() - 1;
            if (newSize > index) {
                for (int i = index, ir = newSize - index; i < ir; ++i) {
                    this.elements.writeInt(i, this.elements.readInt(i + 1));
                }
            }
            this.elements.writeInt(newSize, -1);
            this.size.set(newSize);
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    @Override
    public int size() throws IOException {
        return this.size.getAcquire();
    }

    @Override
    public Cursor<Integer> cursor() throws IOException {
        try {
            return new IntCursor(this, this.get(0), 0);
        } catch (final IndexOutOfBoundsException ex) {
            return Cursor.empty();
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        try {
            return new Cursor.CursorAsIterator<>(this.cursor());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final class IntCursor implements Cursor<Integer> {
        private final IntVector vector;
        private final int element;
        private final int index;

        private IntCursor(final IntVector vector, final int element, final int index) {
            this.vector = vector;
            this.element = element;
            this.index = index;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Integer element() {
            return this.element;
        }

        @Override
        public Cursor<Integer> next() {
            final int i = this.index;
            final StampedLock lock = this.vector.lock;
            long stamp = lock.tryOptimisticRead();
            try {
                for (int spin = 0; ; ++spin) {
                    stamp = spin < SPINS
                            ? lock.tryOptimisticRead()
                            : lock.readLock();
                    if (stamp == 0L) {
                        Thread.onSpinWait();
                        continue;
                    }
                    final int n = this.vector.size.get();
                    final Integer next = i < n ? this.vector.elements.readInt(i) : null;
                    if (lock.validate(stamp)) {
                        return next != null
                                ? new IntCursor(this.vector, next, this.index + 1)
                                : Cursor.empty();
                    }
                }
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                if (StampedLock.isReadLockStamp(stamp)) {
                    lock.unlockRead(stamp);
                }
            }
        }
    }


    @Override
    public String toString() {
        return StreamSupport
                .stream(this.spliterator(), false)
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (final int x : this) {
            hashCode = 31 * hashCode + x;
        }
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Iterable<?>)) {
            return false;
        }
        final Iterator<Integer> e1 = this.iterator();
        final Iterator<?> e2 = ((List<?>) o).iterator();
        while (e1.hasNext() && e2.hasNext()) {
            final Integer o1 = e1.next();
            final Object o2 = e2.next();
            if (!(Objects.equals(o1, o2))) {
                return false;
            }
        }
        return !(e1.hasNext() || e2.hasNext());
    }
}
