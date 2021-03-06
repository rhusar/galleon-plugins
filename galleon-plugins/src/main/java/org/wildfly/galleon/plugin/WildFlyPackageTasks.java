/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.util.CollectionUtils;
import org.wildfly.galleon.plugin.config.FileFilter;
import org.wildfly.galleon.plugin.config.FilePermission;
import org.wildfly.galleon.plugin.config.WildFlyPackageTasksParser;


/**
 *
 * @author Alexey Loubyansky
 */
public class WildFlyPackageTasks {

    public static class Builder {

        private List<FilePermission> filePermissions = Collections.emptyList();
        private List<String> mkDirs = Collections.emptyList();
        private List<FileFilter> windowsLineEndFilters = Collections.emptyList();
        private List<FileFilter> unixLineEndFilters = Collections.emptyList();

        private List<WildFlyPackageTask> tasks = Collections.emptyList();

        private Builder() {
        }

        public Builder addTask(WildFlyPackageTask task) {
            tasks = CollectionUtils.add(tasks, task);
            return this;
        }

        public Builder addFilePermissions(FilePermission filePermission) {
            filePermissions = CollectionUtils.add(filePermissions, filePermission);
            return this;
        }

        public Builder addMkDir(String mkdirs) {
            mkDirs = CollectionUtils.add(mkDirs, mkdirs);
            return this;
        }

        public Builder addWindowsLineEndFilter(FileFilter filter) {
            windowsLineEndFilters = CollectionUtils.add(windowsLineEndFilters, filter);
            return this;
        }

        public Builder addUnixLineEndFilter(FileFilter filter) {
            unixLineEndFilters = CollectionUtils.add(unixLineEndFilters, filter);
            return this;
        }

        public WildFlyPackageTasks build() {
            return new WildFlyPackageTasks(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static WildFlyPackageTasks load(Path configFile) throws ProvisioningException {
        try (InputStream configStream = Files.newInputStream(configFile)) {
            return new WildFlyPackageTasksParser().parse(configStream);
        } catch (XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(configFile), e);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.openFile(configFile), e);
        }
    }

    private final List<FilePermission> filePermissions;
    private final List<String> mkDirs;
    private final List<FileFilter> windowsLineEndFilters;
    private final List<FileFilter> unixLineEndFilters;
    private final List<WildFlyPackageTask> tasks;

    private WildFlyPackageTasks(Builder builder) {
        this.filePermissions = CollectionUtils.unmodifiable(builder.filePermissions);
        this.mkDirs = CollectionUtils.unmodifiable(builder.mkDirs);
        this.windowsLineEndFilters = CollectionUtils.unmodifiable(builder.windowsLineEndFilters);
        this.unixLineEndFilters = CollectionUtils.unmodifiable(builder.unixLineEndFilters);
        this.tasks = CollectionUtils.unmodifiable(builder.tasks);
    }

    public boolean hasFilePermissions() {
        return !filePermissions.isEmpty();
    }

    public List<FilePermission> getFilePermissions() {
        return filePermissions;
    }

    public boolean hasMkDirs() {
        return !mkDirs.isEmpty();
    }

    public List<String> getMkDirs() {
        return mkDirs;
    }

    public List<FileFilter> getWindowsLineEndFilters() {
        return windowsLineEndFilters;
    }

    public List<FileFilter> getUnixLineEndFilters() {
        return unixLineEndFilters;
    }

    public boolean hasTasks() {
        return !tasks.isEmpty();
    }

    public List<WildFlyPackageTask> getTasks() {
        return tasks;
    }
}
