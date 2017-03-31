package com.dx168.fastdex.build.snapshoot.sourceset;

import com.dx168.fastdex.build.snapshoot.api.DiffInfo;
import com.dx168.fastdex.build.snapshoot.api.ResultSet;
import com.dx168.fastdex.build.snapshoot.file.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by tong on 17/3/30.
 */
public class JavaDirectorySnapshoot extends DirectorySnapshoot {
    private static final FileSuffixFilter JAVA_SUFFIX_FILTER = new FileSuffixFilter(".java");

    public JavaDirectorySnapshoot() {
    }

    public JavaDirectorySnapshoot(JavaDirectorySnapshoot snapshoot) {
        super(snapshoot);
    }

    public JavaDirectorySnapshoot(File directory) throws IOException {
        super(directory, JAVA_SUFFIX_FILTER);
    }

    @Override
    protected ResultSet createEmptyResultSet() {
        return new JavaDirectoryResultSet();
    }

    @Override
    protected DiffInfo createEmptyDiffInfo() {
        return new JavaFileDiffInfo();
    }
}