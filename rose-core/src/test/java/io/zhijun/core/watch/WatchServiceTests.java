package io.zhijun.core.watch;

import io.zhijun.core.spi.SpiLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WatchServiceTests {

    @Test
    void defaultsReturnsNonNullInstance() {
        assertNotNull(WatchService.defaults());
    }

    @Test
    void defaultsReturnsClosableInstance() throws Exception {
        try (WatchService ws = WatchService.defaults()) {
            assertNotNull(ws);
        }
    }

    @Test
    void watchAndCloseLifecycle(@TempDir Path tempDir) throws Exception {
        WatchService ws = WatchService.defaults();
        Path dir = tempDir.resolve("watch-lifecycle");
        Files.createDirectories(dir);
        File file = dir.resolve("test.yaml").toFile();
        assertTrue(file.createNewFile());

        // watch, start, then close cleanly
        CountDownLatch latch = new CountDownLatch(1);
        ws.watch(file, event -> latch.countDown());
        ws.start();
        ws.close();

        // after close, no more events should be fired
        Files.write(file.toPath(), "update".getBytes(StandardCharsets.UTF_8));
        assertFalse(latch.await(2, TimeUnit.SECONDS));
    }

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("expected false but was true");
        }
    }

    @Test
    void loadAllFromSpiReturnsDefaultImplementation() {
        List<WatchService> implementations = SpiLoader.defaults().loadAll(WatchService.class);
        for (WatchService ws : implementations) {
            assertNotNull(ws);
        }
    }
}
