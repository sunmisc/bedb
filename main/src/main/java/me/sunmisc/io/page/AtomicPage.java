package me.sunmisc.io.page;

import java.io.IOException;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;

public interface AtomicPage extends Page {

    long caeLong(int index, long expectedValue, long newValue) throws IOException;

    int caeInt(int index, int expectedValue, int newValue) throws IOException;

    default long computeLong(final int index, final LongUnaryOperator accumulate) throws IOException {
        while (true) {
            final long val = this.readLong(index);
            final long next = accumulate.applyAsLong(val);
            if (this.caeLong(index, val, next) == val) {
                return next;
            }
        }
    }
    default long computeInt(final int index, final IntUnaryOperator accumulate) throws IOException {
        while (true) {
            final int val = this.readInt(index);
            final int next = accumulate.applyAsInt(val);
            if (this.caeInt(index, val, next) == val) {
                return next;
            }
        }
    }
}
