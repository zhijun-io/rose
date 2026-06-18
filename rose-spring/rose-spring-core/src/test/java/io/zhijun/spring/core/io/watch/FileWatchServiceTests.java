package io.zhijun.spring.core.io.watch;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileWatchServiceTests {

    @Test
    void shouldDispatchEventsThroughListener() throws Exception {
        File dir = Files.createTempDirectory("rose-watch").toFile();
        File file = new File(dir, "app.properties");
        List<FileChangedEvent> events = new ArrayList<FileChangedEvent>();
        FakeFileWatchService watchService = new FakeFileWatchService();

        watchService.watch(file, new FileChangedListener() {
            @Override
            public void onFileChanged(FileChangedEvent event) {
                events.add(event);
            }
        });
        watchService.publish(file, FileChangedEvent.Kind.MODIFIED);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getFile()).isEqualTo(file);
        assertThat(events.get(0).getKind()).isEqualTo(FileChangedEvent.Kind.MODIFIED);
    }
}
