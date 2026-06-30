package io.zhijun.core.watch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zhijun.core.spi.SpiLoader;
import io.zhijun.core.watch.FileChangedEvent.Kind;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WatchServiceTests {

    @Test
    void defaultsReturnsNonNullInstance() {
        assertNotNull(WatchService.defaults());
    }

    @Test
    void defaultsReturnsRunningWatchService(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("test.properties").toFile();
        assertTrue(file.createNewFile());

        List<FileChangedEvent> events = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        WatchService ws = WatchService.defaults();
        ws.watch(file, event -> {
            events.add(event);
            latch.countDown();
        });
        ws.start();

        // modify the file to trigger a watch event
        Files.write(file.toPath(), "key=value".getBytes(StandardCharsets.UTF_8));
        assertTrue(latch.await(5, TimeUnit.SECONDS), "expected file change event within 5s");

        assertEquals(1, events.size());
        assertEquals(Kind.MODIFIED, events.get(0).getKind());
        assertEquals(file, events.get(0).getFile());

        ws.close();
    }

    @Test
    void watchFiresCreatedEvent(@TempDir Path tempDir) throws Exception {
        Path dir = tempDir.resolve("sub");
        Files.createDirectories(dir);
        Path filePath = dir.resolve("new-file.txt");
        File file = filePath.toFile();

        List<FileChangedEvent> events = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        WatchService ws = WatchService.defaults();
        ws.watch(file, event -> {
            events.add(event);
            latch.countDown();
        });
        ws.start();

        // create the file
        assertTrue(file.createNewFile());
        assertTrue(latch.await(5, TimeUnit.SECONDS), "expected create event within 5s");

        assertEquals(1, events.size());
        assertEquals(Kind.CREATED, events.get(0).getKind());

        ws.close();
    }

    @Test
    void loadAllFromSpiReturnsDefaultImplementation() {
        List<WatchService> implementations = SpiLoader.defaults().loadAll(WatchService.class);
        for (WatchService ws : implementations) {
            assertNotNull(ws);
        }
    }
}
