/*
 * Copyright (c) 2016, Stein Eldar Johnsen
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
package net.morimekta.providence.config;

import net.morimekta.config.Config;
import net.morimekta.providence.PMessage;
import net.morimekta.providence.descriptor.PField;

import static net.morimekta.providence.config.ProvidenceConfigUtil.asMessage;

/**
 * Providence enhanced config interface.
 */
public interface ProvidenceConfig extends Config {
    /**
     * Get value as a providence message.
     *
     * @param key The key to look for.
     * @param <Message> The message type.
     * @param <Field> The message field type.
     * @return The message.
     */
    default <Message extends PMessage<Message, Field>, Field extends PField>
    Message getMessage(String key) {
        return asMessage(getValue(key));
    }
}