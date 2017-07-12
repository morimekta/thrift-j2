/*
 * Copyright 2016 Providence Authors
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
package net.morimekta.providence.reflect.util;

import net.morimekta.providence.descriptor.PDeclaredDescriptor;
import net.morimekta.providence.descriptor.PDescriptorProvider;
import net.morimekta.providence.descriptor.PService;
import net.morimekta.providence.descriptor.PServiceProvider;
import net.morimekta.providence.reflect.contained.CProgram;
import net.morimekta.providence.util.TypeRegistry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.morimekta.providence.reflect.util.ReflectionUtils.programNameFromPath;

/**
 * Program scope type registry.
 */
public class ProgramRegistry implements TypeRegistry {
    private final Map<String, ProgramTypeRegistry> registryMap;

    public ProgramRegistry() {
        registryMap = new HashMap<>();
    }

    @Nonnull
    public ProgramTypeRegistry registryForPath(String path) {
        return registryMap.computeIfAbsent(path, p -> new ProgramTypeRegistry(programNameFromPath(path)));
    }

    /**
     * Gets the document for a given file path.
     *
     * @param path The file path.
     * @return The contained document, or null if not found.
     */
    public boolean containsProgramPath(String path) {
        return registryForPath(path).getProgram() != null;
    }

    public void putProgram(String path, CProgram program) {
        registryForPath(path).setProgram(program);
    }

    @Nonnull
    @Override
    public <T extends PDeclaredDescriptor<T>> T getDeclaredType(@Nonnull String typeName,
                                                                @Nonnull String programContext) {
        return handle(typeName, programContext,
                      p -> p.getDeclaredType(typeName, programContext));
    }

    @Nonnull
    @Override
    public PService getService(String serviceName, String programContext) {
        return handle(serviceName, programContext,
                      r -> r.getService(serviceName, programContext));
    }

    @Override
    public PDescriptorProvider getProvider(String typeName, String programContext, Map<String, String> annotations) {
        return handle(typeName, programContext,
                      r -> r.getProvider(typeName, programContext, annotations));
    }

    @Override
    public PServiceProvider getServiceProvider(String serviceName, String programContext) {
        return handle(serviceName, programContext,
                      r -> r.getServiceProvider(serviceName, programContext));
    }

    private <T> T handle(String typeName, String programContext, Function<ProgramTypeRegistry, T> f) {
        Exception e = null;
        String context = getProgramContext(typeName, programContext);

        for (ProgramTypeRegistry registry : registryMap.values()) {
            if (registry.getLocalProgramContext().equals(context)) {
                try {
                    return f.apply(registry);
                } catch (Exception ignored) {
                    e = ignored;
                }
            }
        }
        if (e != null) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        throw new IllegalArgumentException("No program \"" + context + "\" found for type \"" + typeName + "\"");
    }

    private String getProgramContext(String typeName, String programContext) {
        if (typeName.contains(".")) {
            return typeName.substring(0, typeName.indexOf("."));
        }
        return programContext;
    }
}
