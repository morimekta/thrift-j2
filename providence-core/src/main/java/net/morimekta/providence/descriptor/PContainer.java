/*
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

package net.morimekta.providence.descriptor;

/**
 * Generic descriptor for a container type.
 * <p>
 * See {@link PList}, {@link PSet} and {@link PMap} which specializes for each
 * type of container.
 */
public abstract class PContainer<I, C> implements PDescriptor<C> {
    private final PDescriptorProvider<I> itemDescriptorProvider;

    protected PContainer(PDescriptorProvider<I> provider) {
        itemDescriptorProvider = provider;
    }

    public PDescriptor<I> itemDescriptor() {
        return itemDescriptorProvider.descriptor();
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public String toString() {
        return getQualifiedName(null);
    }
}
