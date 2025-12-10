package me.sunmisc.io.page;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ConcurrentPage implements AtomicPage {
    private static final VarHandle AA = MethodHandles.arrayElementVarHandle(Lock[].class);
    private static final int NCPU = Runtime.getRuntime().availableProcessors();
    private static final Lock[] EMPTY = new Lock[0];

    private final AtomicReference<Lock[]> cells = new AtomicReference<>(EMPTY);
    private final AtomicBoolean busy = new AtomicBoolean();
    private final Page origin;

    public ConcurrentPage(final Page origin) {
        this.origin = origin;
    }

    private static int hash(final int key) {
        return (key ^ (key >>> 16));
    }

    private Lock cellLock(final int index) {
        final int hash = hash(index);
        Lock cell;
        for (boolean collide = false;;) {
            Lock[] cs = this.cells.get();
            if (cs == EMPTY) {
                final Lock[] rs = new Lock[2];
                final Lock first = new ReentrantLock();
                rs[hash & 1] = first;
                final Lock[] w = this.cells.compareAndExchange(EMPTY, rs);
                if (w == EMPTY) {
                    cell = first;
                    break;
                } else {
                    cs = w;
                }
            }
            final int n = cs.length;
            final int h = hash & (n - 1);
            cell = cs[h];
            if (cell == null) {
                final Lock rs = new ReentrantLock();
                final Lock w = (Lock) AA.compareAndExchange(cs, h, null, rs);
                if (w == null) {
                    cell = rs;
                    break;
                } else {
                    cell = w;
                }
            }
            if (collide || n >= NCPU) {
                break;
            } else if (cell.tryLock()) {
                return cell;
            } else if (!this.busy.get() && this.busy.compareAndSet(false, true)) {
                try {
                    if (this.cells.get() == cs) {
                        this.cells.set(Arrays.copyOf(cs, n << 1));
                    }
                } finally {
                    this.busy.set(false);
                    collide = true;
                }
            }
            Thread.onSpinWait();
        }
        cell.lock();
        return cell;
    }

    @Override
    public boolean casLong(final int index, final long expectedValue, final long newValue) throws IOException {
        final Lock lock = this.cellLock(index);
        try {
            if (this.origin.readLong(index) == expectedValue) {
                this.origin.writeLong(index, newValue);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean casInt(final int index, final int expectedValue, final int newValue) throws IOException {
        final Lock lock = this.cellLock(index);
        try {
            if (this.origin.readInt(index) == expectedValue) {
                this.origin.writeInt(index, newValue);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void writeInt(final int index, final int value) throws IOException {
        final Lock lock = this.cellLock(index);
        try {
            this.origin.writeInt(index, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void writeLong(final int index, final long value) throws IOException {
        final Lock lock = this.cellLock(index);
        try {
            this.origin.writeLong(index, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int readInt(final int index) throws IOException {
        return this.origin.readInt(index);
    }

    @Override
    public long readLong(final int index) throws IOException {
        return this.origin.readLong(index);
    }

    @Override
    public int length() {
        return this.origin.length();
    }

    @Override
    public long offset() {
        return this.origin.offset();
    }
}
