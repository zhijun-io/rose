package io.zhijun.spring.property;

import io.zhijun.core.watch.FileChangedEvent;
import io.zhijun.core.watch.FileChangedListener;
import io.zhijun.core.watch.FileWatchService;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

class FakeFileWatchService implements FileWatchService {

    private final Map<File, FileChangedListener> listeners = new LinkedHashMap<>();

    @Override
    public void watch(File file, FileChangedListener listener) {
        listeners.put(file, listener);
    }

    @Override
    public void start() {}

    @Override
    public void close() {}

    void publish(File file, FileChangedEvent.Kind kind) {
        FileChangedListener listener = listeners.get(file);
        if (listener != null) {
            listener.onFileChanged(new FileChangedEvent(kind, file));
        }
    }
}
