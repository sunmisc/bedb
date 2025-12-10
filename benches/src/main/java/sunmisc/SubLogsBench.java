package sunmisc;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.numberOfLeadingZeros;

@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(3)
@Threads(1)
@BenchmarkMode({Mode.Throughput})
@State(Scope.Thread)
public class SubLogsBench {

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(SubLogsBench.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
    private Random random;

    @Setup
    public void init() {
        this.random = new Random(884);
    }

    private static int log_2(final int x) {
        return 31 - numberOfLeadingZeros(x);
    }

    @Benchmark
    public int logOfQuotient() {
        final int x = this.random.nextInt(2, 1_000_000);
        final int y = this.random.nextInt(2, 1_000_000);
        return log_2((x + y) / x);
    }

    @Benchmark
    public int differenceOfLogs() {
        final int x = this.random.nextInt(2, 1_000_000);
        final int y = this.random.nextInt(2, 1_000_000);
        return log_2(x + y) - log_2(x);
    }
}
