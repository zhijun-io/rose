package io.zhijun.core.watch.internal;

import io.zhijun.core.spi.annotation.Priority;
import io.zhijun.core.spi.annotation.SpiImpl;
import io.zhijun.core.watch.FileChangedEvent;
import io.zhijun.core.watch.FileChangedListener;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple file watch service backed by java.nio.file.WatchService.
 *
 * <p>线程安全：所有公共方法可在任意线程安全调用。{@code watch()} 支持在
 * {@code start()} 之前或之后调用。
 */
@SpiImpl
@Priority(Integer.MAX_VALUE)
public class StandardWatchService implements io.zhijun.core.watch.WatchService {

    private final WatchService watchService;

    // 父目录 -> WatchKey（用于线程安全注册去重）
    private final ConcurrentHashMap<Path, WatchKey> watchedDirectories = new ConcurrentHashMap<>();

    // WatchKey -> 父目录（用于 loop() 线程快速查找）
    private final ConcurrentHashMap<WatchKey, Path> watchKeys = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Path, FileChangedListener> listeners = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile Thread worker;

    public StandardWatchService() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    @Override
    public void watch(File file, FileChangedListener listener) throws IOException {
        Path filePath = file.toPath().toAbsolutePath().normalize();
        Path parent = filePath.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("file has no parent directory: " + file);
        }
        listeners.put(filePath, listener);
        // computeIfAbsent 保证每个父目录只会注册一次，原子操作
        try {
            watchedDirectories.computeIfAbsent(parent, p -> {
                try {
                    WatchKey key = p.register(
                            watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE);
                    watchKeys.put(key, p);
                    return key;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            Thread t = new Thread(this::loop, "rose-file-watch-service");
            t.setDaemon(true);
            worker = t;
            t.start();
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

    private static FileChangedEvent.Kind toKind(WatchEvent.Kind<?> kind) {
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            return FileChangedEvent.Kind.CREATED;
        }
        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            return FileChangedEvent.Kind.DELETED;
        }
        return FileChangedEvent.Kind.MODIFIED;
    }

    @Override
    public void close() throws IOException {
        running.set(false);
        watchService.close();
        Thread w = worker;
        if (w != null) {
            w.interrupt();
        }
    }
}
