package sunmisc.io;

import me.sunmisc.io.page.HeapPage;
import me.sunmisc.io.page.Page;
import me.sunmisc.io.page.SegmentsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;

public class SegmentsPageTest {

    @Test
    public void segmentsIndexationTest() throws IOException {
        final int size = 1024 * 10;
        final SegmentsPage page = new SegmentsPage(
                IntStream
                        .iterate(4,
                                value -> value <= size,
                                operand -> operand << 1)
                        .mapToObj(e -> (Page)new HeapPage(0, e))
                        .toList()
        );
        final Random write = new Random(23);
        for (int i = 0; i < size; ++i) {
            final int v = write.nextInt(0, size);
            page.writeInt(v, v);
        }
        final Random read = new Random(23);
        for (int i = 0; i < size; ++i) {
            final int v = read.nextInt(0, size);
            Assert.assertEquals(page.readInt(v), v);
        }
    }
}
