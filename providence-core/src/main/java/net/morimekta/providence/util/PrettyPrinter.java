/*
 * Copyright 2015-2016 Providence Authors
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
package net.morimekta.providence.util;

import net.morimekta.providence.PMessage;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.providence.serializer.PrettySerializer;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Pretty printer that can print message content for easily reading and
 * debugging.
 */
public class PrettyPrinter {
    private static final PrettySerializer DEBUG_STRING_SERIALIZER = new PrettySerializer("  ", " ", "\n", "", false, true);

    /**
     * Prints a pretty formatted string that is optimized for diffing (mainly
     * for testing and debugging).
     *
     * @param message The message to stringify.
     * @param <Message> The message type.
     * @return The resulting string.
     */
    public static <Message extends PMessage<Message, Field>, Field extends PField>
    String debugString(Message message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DEBUG_STRING_SERIALIZER.serialize(baos, message);
        return new String(baos.toByteArray(), UTF_8);
    }
}
