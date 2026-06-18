package io.zhijun.spring.core.io.watch;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class FakeFileWatchService implements FileWatchService {

    private final Map<File, FileChangedListener> listeners = new LinkedHashMap<File, FileChangedListener>();

    @Override
    public void watch(File file, FileChangedListener listener) throws IOException {
        listeners.put(file, listener);
    }

    @Override
    public void start() {
    }

    @Override
    public void close() throws IOException {
    }

    public void publish(File file, FileChangedEvent.Kind kind) {
        FileChangedListener listener = listeners.get(file);
        if (listener != null) {
            listener.onFileChanged(new FileChangedEvent(kind, file));
        }
    }
}
