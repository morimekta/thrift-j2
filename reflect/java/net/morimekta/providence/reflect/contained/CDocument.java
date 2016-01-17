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

package net.morimekta.providence.reflect.contained;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.morimekta.providence.descriptor.PDeclaredDescriptor;
import net.morimekta.providence.descriptor.PField;

/**
 * Contained document. It contains everything that is parsed out of a single
 * thrift file.
 */
@SuppressWarnings("unused")
public class CDocument {
    private final String                       mComment;
    private final String                       mPackageName;
    private final List<String>                 mIncludes;
    private final Map<String, String>          mNamespaces;
    private final Map<String, String>          mTypedefs;
    private final List<PDeclaredDescriptor<?>> mDeclaredTypes;
    private final List<PField<?>>              mConstants;

    public CDocument(String comment,
                     String packageName,
                     Map<String, String> namespaces,
                     List<String> includes,
                     Map<String, String> typedefs,
                     List<PDeclaredDescriptor<?>> declaredTypes,
                     List<PField<?>> constants) {
        mComment = comment;
        mPackageName = packageName;
        mNamespaces = Collections.unmodifiableMap(namespaces);
        mIncludes = Collections.unmodifiableList(includes);
        mTypedefs = Collections.unmodifiableMap(typedefs);
        mDeclaredTypes = Collections.unmodifiableList(declaredTypes);
        mConstants = Collections.unmodifiableList(constants);
    }

    public String getComment() {
        return mComment;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public List<String> getIncludes() {
        return mIncludes;
    }

    public Map<String, String> getNamespaces() {
        return mNamespaces;
    }

    public Map<String, String> getTypedefs() {
        return mTypedefs;
    }

    public List<PDeclaredDescriptor<?>> getDeclaredTypes() {
        return mDeclaredTypes;
    }

    public List<PField<?>> getConstants() {
        return mConstants;
    }

    // --- Extra methods.

    public String getNamespaceForLanguage(String language) {
        return mNamespaces.get(language);
    }

}