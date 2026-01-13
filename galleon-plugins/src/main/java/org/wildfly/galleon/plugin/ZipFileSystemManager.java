/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.galleon.plugin;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe manager for creating and accessing ZIP file systems.
 * This class ensures that multiple threads can safely access ZIP file systems
 * without encountering ClosedFileSystemException or FileSystemAlreadyExistsException.
 *
 * @author Radoslav Husar
 */
public class ZipFileSystemManager {

    private static final ConcurrentHashMap<Path, Lock> FILE_LOCKS = new ConcurrentHashMap<>();

    /**
     * Executes an operation on a ZIP file system in a thread-safe manner.
     * This method ensures that only one thread at a time can create and access
     * a FileSystem for a given ZIP file path.
     *
     * @param <T> the type of result returned by the operation
     * @param zipPath the path to the ZIP file
     * @param operation the operation to perform on the file system
     * @return the result of the operation
     * @throws IOException if an I/O error occurs
     */
    public static <T> T withFileSystem(Path zipPath, FileSystemFunction<T> operation) throws IOException {
        // Get or create a lock for this specific zip file path
        Lock lock = FILE_LOCKS.computeIfAbsent(zipPath.toAbsolutePath().normalize(), k -> new ReentrantLock());

        lock.lock();
        try {
            // Create the file system within the lock to prevent concurrent access
            try (FileSystem fs = FileSystems.newFileSystem(zipPath, (ClassLoader) null)) {
                return operation.apply(fs);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Functional interface for operations that need to be performed on a FileSystem.
     *
     * @param <T> the type of result returned by the operation
     */
    @FunctionalInterface
    public interface FileSystemFunction<T> {
        T apply(FileSystem fs) throws IOException;
    }
}
