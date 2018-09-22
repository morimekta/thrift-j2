/*
 * Copyright 2016,2017 Providence Authors
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.morimekta.providence.config.impl;

import net.morimekta.providence.PMessage;
import net.morimekta.providence.config.ConfigListener;
import net.morimekta.providence.config.ConfigSupplier;
import net.morimekta.providence.config.ProvidenceConfigException;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.util.FileWatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.time.Clock;

/**
 * A supplier to get a config (aka message) from an intermediate config. This is
 * essentially the initiator for the config. It will always have a config
 * message instance, and will log (error) if it later fails to load an updated
 * config.
 */
public class IntermediateConfigSupplier<Message extends PMessage<Message, Field>, Field extends PField>
        extends UpdatingConfigSupplier<Message, Field> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntermediateConfigSupplier.class);

    private final File                           configFile;
    private final IntermediateConfigParser       configParser;
    private final ConfigListener<Message, Field> configListener;
    private final FileWatcher.Watcher            fileListener;
    private final ConfigSupplier<Message, Field> parentSupplier;

    public IntermediateConfigSupplier(@Nonnull File configFile,
                                      @Nullable ConfigSupplier<Message, Field> parentSupplier,
                                      @Nullable FileWatcher fileWatcher,
                                      @Nonnull IntermediateConfigParser configParser,
                                      @Nonnull Clock clock)
            throws ProvidenceConfigException {
        super(clock);
        this.configFile = configFile;
        this.configParser = configParser;
        this.parentSupplier = parentSupplier;

        synchronized (this) {
            if (fileWatcher != null) {
                fileWatcher.startWatching(configFile);
                // TODO: Make the file watcher hold weak references.
                // This may cause long term memory leaks.
                fileListener = file -> {
                    if (configFile.equals(file)) {
                        reload();
                    }
                };
                fileWatcher.weakAddWatcher(fileListener);
            } else {
                fileListener = null;
            }

            if (parentSupplier != null) {
                this.configListener = config -> this.reload();
                this.parentSupplier.addListener(configListener);
                set(loadConfig(parentSupplier.get()));
            } else {
                this.configListener = null;
                set(loadConfig(null));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IntermediateConfig{")
               .append(configFile.getName());
        if (parentSupplier != null) {
            builder.append(", parent=");
            builder.append(parentSupplier.toString());
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public String getName() {
        return "IntermediateConfig{" + configFile.getName() + "}";
    }

    /**
     * Trigger reloading of the config file.
     */
    private void reload() {
        try {
            LOGGER.trace("Config reload triggered for " + configFile);
            if (parentSupplier != null) {
                set(loadConfig(parentSupplier.get()));
            } else {
                set(loadConfig(null));
            }
        } catch (ProvidenceConfigException e) {
            LOGGER.error("Exception when reloading " + configFile, e);
        }
    }

    @Nonnull
    private Message loadConfig(@Nullable Message parent) throws ProvidenceConfigException {
        return configParser.parseConfig(configFile.toPath(), parent);
    }
}
