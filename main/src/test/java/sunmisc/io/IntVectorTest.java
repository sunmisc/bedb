package sunmisc.io;

import me.sunmisc.io.IntVector;
import me.sunmisc.io.alloc.AllocAlignedPage;
import me.sunmisc.io.alloc.AllocFileDefault;
import me.sunmisc.io.alloc.AllocIntPage;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntVectorTest {
    private static final int FICTITIOUS = 32;
    private IntVector concurrent;
    private List<Integer> cow;

    @BeforeTest
    public void setup() throws IOException {
        this.concurrent = new IntVector(
                new AllocAlignedPage(
                        new AllocIntPage(
                                new AllocFileDefault(
                                        new File("list.bin")
                                )
                        )
                )
        );
        this.cow = new CopyOnWriteArrayList<>();
        for (int i = 0; i < FICTITIOUS; ++i) {
            this.concurrent.add(i);
            this.cow.add(i);
        }
    }

    @Test(invocationCount = 10)
    public void modifyToList() throws IOException {
        try (final ExecutorService executor = Executors.newSingleThreadExecutor()) {
            for (int a = FICTITIOUS; a < 1000; ++a) {
                final int q = a;
                executor.execute(() -> {
                    try {
                        this.cow.add(q);
                        this.concurrent.add(q);
                    } catch (final IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
            for (int i = 0; i < FICTITIOUS; ++i) {
                this.cow.removeFirst();
                this.concurrent.remove(0);
            }
        }
        System.out.println(this.concurrent);
        Assert.assertEquals(
                this.cow,
                this.concurrent,
                "The lists should be equal after concurrent operations"
        );
        Assert.assertEquals(
                this.cow.size(),
                this.concurrent.size(),
                "The size should match the expected value"
        );
    }
}
