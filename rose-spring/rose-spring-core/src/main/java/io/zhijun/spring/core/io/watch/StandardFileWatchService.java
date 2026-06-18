package io.zhijun.spring.core.io.watch;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple file watch service backed by Java NIO WatchService.
 */
public class StandardFileWatchService implements FileWatchService {

    private final WatchService watchService;

    private final Map<WatchKey, Path> watchKeys = new HashMap<WatchKey, Path>();

    private final Map<Path, FileChangedListener> listeners = new ConcurrentHashMap<Path, FileChangedListener>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private Thread worker;

    public StandardFileWatchService() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    @Override
    public synchronized void watch(File file, FileChangedListener listener) throws IOException {
        Path filePath = file.toPath().toAbsolutePath().normalize();
        Path parent = filePath.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("file has no parent directory: " + file);
        }
        listeners.put(filePath, listener);
        if (!watchKeys.containsValue(parent)) {
            WatchKey key = parent.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            watchKeys.put(key, parent);
        }
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    loop();
                }
            }, "rose-file-watch-service");
            worker.setDaemon(true);
            worker.start();
        }
    }

    private void loop() {
        while (running.get()) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (ClosedWatchServiceException e) {
                return;
            }

            Path directory = watchKeys.get(key);
            if (directory != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    Path relative = (Path) event.context();
                    Path filePath = directory.resolve(relative).toAbsolutePath().normalize();
                    FileChangedListener listener = listeners.get(filePath);
                    if (listener != null) {
                        listener.onFileChanged(new FileChangedEvent(toKind(kind), filePath.toFile()));
                    }
                }
            }
            key.reset();
        }
    }

    private FileChangedEvent.Kind toKind(WatchEvent.Kind<?> kind) {
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            return FileChangedEvent.Kind.CREATED;
        }
        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            return FileChangedEvent.Kind.DELETED;
        }
        return FileChangedEvent.Kind.MODIFIED;
    }

    @Override
    public synchronized void close() throws IOException {
        running.set(false);
        watchService.close();
        if (worker != null) {
            worker.interrupt();
        }
    }
}
