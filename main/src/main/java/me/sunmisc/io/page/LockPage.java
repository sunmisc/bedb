package me.sunmisc.io.page;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class LockPage implements AtomicPage {
    private final Page origin;
    private final Lock lock = new ReentrantLock();

    public LockPage(final Page origin) {
        this.origin = origin;
    }

    @Override
    public boolean casLong(final int index, final long expectedValue, final long newValue) throws IOException {
        this.lock.lock();
        try {
            if (this.origin.readLong(index) == expectedValue) {
                this.origin.writeLong(index, newValue);
                return true;
            }
            return false;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean casInt(final int index, final int expectedValue, final int newValue) throws IOException {
        this.lock.lock();
        try {
            if (this.origin.readInt(index) == expectedValue) {
                this.origin.writeInt(index, newValue);
                return true;
            }
            return false;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void writeInt(final int index, final int value) throws IOException {
        this.lock.lock();
        try {
            this.origin.writeInt(index, value);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void writeLong(final int index, final long value) throws IOException {
        this.lock.lock();
        try {
            this.origin.writeLong(index, value);
        } finally {
            this.lock.unlock();
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