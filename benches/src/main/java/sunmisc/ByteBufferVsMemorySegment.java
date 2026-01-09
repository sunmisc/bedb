package sunmisc;

import me.sunmisc.io.page.BufferPage;
import me.sunmisc.io.page.FFilePage;
import me.sunmisc.io.page.Page;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 8, time = 1)
@State(Scope.Benchmark)
public class ByteBufferVsMemorySegment {
    private static final int SIZE = 1024;

    @Param
    private Kind kind;
    private Page page;

    public enum Kind { BUFFER, FOREIGN }


    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ByteBufferVsMemorySegment.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void init() {
        this.page = switch (this.kind) {
            case BUFFER -> new BufferPage(0, ByteBuffer.allocate(SIZE));
            case FOREIGN -> new FFilePage(Arena.global().allocate(SIZE), 0);
        };
    }

    @Benchmark
    public long read64() throws IOException {
        return page.readLong(0);
    }

    @Benchmark
    public long write64() throws IOException {
        final long now = System.currentTimeMillis();
        int n = ThreadLocalRandom.current().nextInt(0, 8);
        page.writeLong(n, now);
        return now;
    }

    @Benchmark
    public int read32() throws IOException {
        return page.readInt(0);
    }

    @Benchmark
    public int write32() throws IOException {
        final int now = (int)System.currentTimeMillis();
        int n = ThreadLocalRandom.current().nextInt(0, 8);
        page.writeInt(n, now);
        return now;
    }
}
