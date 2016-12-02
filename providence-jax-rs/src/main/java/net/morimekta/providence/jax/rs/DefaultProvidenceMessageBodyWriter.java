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
package net.morimekta.providence.jax.rs;

import net.morimekta.providence.serializer.BinarySerializer;
import net.morimekta.providence.serializer.DefaultSerializerProvider;
import net.morimekta.providence.serializer.FastBinarySerializer;
import net.morimekta.providence.serializer.JsonSerializer;

import javax.ws.rs.Produces;

/**
 * Default message body writer for standard providence serializers.
 */
@Produces({
                  BinarySerializer.MIME_TYPE,
                  BinarySerializer.ALT_MIME_TYPE,
                  FastBinarySerializer.MIME_TYPE,
                  JsonSerializer.MIME_TYPE,
                  JsonSerializer.MIME_TYPE
          })
public class DefaultProvidenceMessageBodyWriter extends ProvidenceMessageBodyWriter {
    public DefaultProvidenceMessageBodyWriter() {
        super(new DefaultSerializerProvider());
    }
}
