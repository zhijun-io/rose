package io.zhijun.spring.core.io.watch;

import java.io.File;

/**
 * File system change event.
 */
public class FileChangedEvent {

    public enum Kind {
        CREATED,
        MODIFIED,
        DELETED
    }

    private final Kind kind;

    private final File file;

    public FileChangedEvent(Kind kind, File file) {
        this.kind = kind;
        this.file = file;
    }

    public Kind getKind() {
        return kind;
    }

    public File getFile() {
        return file;
    }
}
