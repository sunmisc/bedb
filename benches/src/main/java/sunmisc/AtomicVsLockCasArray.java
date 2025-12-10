package sunmisc;

import me.sunmisc.io.page.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Threads(8)
@BenchmarkMode({Mode.AverageTime})
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 8, time = 1)
@State(Scope.Benchmark)
public class AtomicVsLockCasArray {

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(AtomicVsLockCasArray.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    private static final int SIZE = 4096;

    @Param
    private Kind kind;
    private AtomicPage page;

    @Setup
    public void prepare() throws IOException {
        final Page heap = new HeapPage(0, SIZE);
        this.page = switch (this.kind) {
            case SHARED -> new LockPage(heap);
            case STRIPED -> new ConcurrentPage(heap);
        };
        for (int i = 0 ; i < SIZE; ++i) {
            this.page.writeInt(i, 1);
        }
    }
    public enum Kind { STRIPED, SHARED }

    @Benchmark
    public int cas() throws IOException {
        final int i = ThreadLocalRandom.current().nextInt(0, SIZE);
        int v;
        do {
            v = this.page.readInt(i);
        } while (!this.page.casInt(i, v, v + i));
        return v;
    }

    @Benchmark
    public int casSpinWait() throws IOException {
        final int i = ThreadLocalRandom.current().nextInt(0, SIZE);
        int v;
        do {
            Thread.onSpinWait();
            v = this.page.readInt(i);
        } while (!this.page.casInt(i, v, v + i));
        return v;
    }
}

