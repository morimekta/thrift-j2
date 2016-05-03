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

package net.morimekta.providence.util;

import net.morimekta.providence.PMessage;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.providence.descriptor.PStructDescriptor;
import net.morimekta.providence.serializer.JsonSerializer;
import net.morimekta.providence.serializer.Serializer;
import net.morimekta.providence.serializer.SerializerException;
import net.morimekta.providence.streams.MessageStreams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Convenience methods for reading providence messages from resources.
 */
public class ProvidenceHelper {
    public static <T extends PMessage<T>, TF extends PField> T
    fromJsonResource(String path, PStructDescriptor<T, TF> descriptor)
            throws SerializerException, IOException {
        return fromResource(path, descriptor, new JsonSerializer(true));
    }

    public static <T extends PMessage<T>, F extends PField> ArrayList<T>
    arrayListFromJsonResource(String path, PStructDescriptor<T, F> descriptor)
            throws SerializerException, IOException {
        return arrayListFromResource(path, descriptor, new JsonSerializer(true));
    }

    public static <T extends PMessage<T>, TF extends PField> T
    fromResource(String resource, PStructDescriptor<T, TF> descriptor, Serializer serializer)
            throws SerializerException, IOException {
        InputStream in = ProvidenceHelper.class.getResourceAsStream(resource);
        if(in == null) {
            throw new IOException("No such resource " + resource);
        }
        return serializer.deserialize(new BufferedInputStream(in), descriptor);
    }

    public static <T extends PMessage<T>, F extends PField> ArrayList<T>
    arrayListFromResource(String path, PStructDescriptor<T, F> descriptor, Serializer serializer)
            throws SerializerException, IOException {
        return (ArrayList<T>) MessageStreams.resource(path, serializer, descriptor)
                                            .collect(Collectors.toList());
    }

    private ProvidenceHelper() {}
}