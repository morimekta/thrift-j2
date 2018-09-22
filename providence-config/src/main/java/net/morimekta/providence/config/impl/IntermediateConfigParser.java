/*
 * Copyright 2017 Providence Authors
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
import net.morimekta.providence.PMessageBuilder;
import net.morimekta.providence.config.ProvidenceConfigException;
import net.morimekta.providence.descriptor.PContainer;
import net.morimekta.providence.descriptor.PDescriptor;
import net.morimekta.providence.descriptor.PEnumDescriptor;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.providence.descriptor.PMap;
import net.morimekta.providence.descriptor.PMessageDescriptor;
import net.morimekta.providence.serializer.pretty.Tokenizer;
import net.morimekta.providence.serializer.pretty.TokenizerException;
import net.morimekta.providence.util.TypeRegistry;
import net.morimekta.util.Binary;
import net.morimekta.util.io.Utf8StreamReader;
import net.morimekta.util.json.JsonException;
import net.morimekta.util.json.JsonToken;
import net.morimekta.util.json.JsonTokenizer;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.canonicalFileLocation;

/**
 * This parser parses config files. The class in itself should be stateless, so
 * can safely be used in multiple threads safely. This is a utility class created
 * in order to simplify testing.
 */
public class IntermediateConfigParser {
    /**
     * Create a providence config parser instance.
     *
     * @param registry The type registry used.
     * @param strict If config should be parsed and handled strictly.
     */
    public IntermediateConfigParser(TypeRegistry registry, boolean strict) {
        this.registry = registry;
        this.strict = strict;
    }

    <M extends PMessage<M, F>, F extends PField>
    M parseConfig(@Nonnull Path configFile, M parent) throws ProvidenceConfigException {
        try {
            configFile = canonicalFileLocation(configFile);
        } catch (IOException e) {
            throw new ProvidenceConfigException(e, "Unable to resolve config file " + configFile)
                    .setFile(configFile.getFileName().toString());
        }
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(configFile.toFile()))) {
            return parseConfigRecursively(in, parent);
        } catch (JsonException e) {
            throw new ProvidenceConfigException(e, e.getMessage())
                    .setFile(configFile.getFileName().toString())
                    .setLine(e.getLine())
                    .setLineNo(e.getLineNo())
                    .setLinePos(e.getLinePos())
                    .setLength(e.getLen());
        } catch (IOException e) {
            if (e instanceof ProvidenceConfigException) {
                ProvidenceConfigException pce = (ProvidenceConfigException) e;
                if (pce.getFile() == null) {
                    pce.setFile(configFile.getFileName().toString());
                }
                throw pce;
            }
            if (e instanceof TokenizerException) {
                TokenizerException te = (TokenizerException) e;
                if (te.getFile() == null) {
                    te.setFile(configFile.getFileName().toString());
                }
                throw new ProvidenceConfigException(te);
            }
            throw new ProvidenceConfigException(e, e.getMessage())
                    .setFile(configFile.getFileName().toString());
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends PMessage<M, F>, F extends PField>
    M parseConfigRecursively(@Nonnull InputStream in, M parent) throws IOException, JsonException {
        // Non-enclosed content, meaning we should read the whole file immediately.
        JsonTokenizer tokenizer = new JsonTokenizer(new Utf8StreamReader(in), Tokenizer.DEFAULT_BUFFER_SIZE);

        tokenizer.expectSymbol("Config start", JsonToken.kMapStart);

        JsonToken token = tokenizer.expectString("Schema name");
        String schema = token.rawJsonLiteral();
        PMessageDescriptor<M, F> descriptor;
        try {
            descriptor = registry.getMessageType(schema);
        } catch (IllegalArgumentException e) {
            throw new JsonException("Unknown schema name " + schema, tokenizer, token);
        }

        tokenizer.expectSymbol("Schema message sep", JsonToken.kKeyValSep);
        tokenizer.expectSymbol("Message start", JsonToken.kMapStart);

        PMessageBuilder<M,F> builder;
        if (parent != null) {
            builder = parent.mutate();
        } else {
            builder = descriptor.builder();
        }
        M result = parseMessage(tokenizer, descriptor, builder);

        tokenizer.expectSymbol("Config end", JsonToken.kMapEnd);

        if (tokenizer.hasNext()) {
            JsonToken next = tokenizer.peek("");
            throw new JsonException("Garbage at end of file", tokenizer, next);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <M extends PMessage<M, F>, F extends PField>
    M parseMessage(@Nonnull JsonTokenizer tokenizer,
                   @Nonnull PMessageDescriptor<M,F> descriptor,
                   @Nonnull PMessageBuilder<M,F> builder) throws IOException, JsonException {
        Map<String,String> annotations = new HashMap<>();

        // Non-enclosed content, meaning we should read the whole file immediately.
        if (tokenizer.peek("Message end or field").isSymbol(JsonToken.kMapEnd)) {
            tokenizer.next();
            return builder.build();
        }
        char sep = JsonToken.kMapStart;
        while (sep != JsonToken.kMapEnd) {
            JsonToken token = tokenizer.expectString("Message field");
            String name = token.rawJsonLiteral();
            if (name.startsWith("@")) {
                // key: value only.
                tokenizer.expectSymbol("Message key value sep", JsonToken.kKeyValSep);
                token = tokenizer.expectString("Annotation value");
                String annotation = token.rawJsonLiteral();
                annotations.put(name, annotation);
            } else {
                PField field = descriptor.findFieldByName(name);
                if (field == null) {
                    if (strict) {
                        throw new JsonException("Unknown field " + name + " in " + descriptor.getQualifiedName(), tokenizer, token);
                    }
                    tokenizer.expectSymbol("Message key value sep", JsonToken.kKeyValSep);
                    consumeIntermediateJsonValue(tokenizer);
                } else {
                    tokenizer.expectSymbol("Message key value sep", JsonToken.kKeyValSep);
                    token = tokenizer.expect("Field value");
                    if (token.isNull()) {
                        builder.clear(field.getId());
                    } else {
                        switch (field.getType()) {
                            case MESSAGE: {
                                PMessageBuilder mb = builder.mutator(field.getId());
                                if (REPLACE.equals(annotations.get(AT_RESOLVE + field.getName()))) {
                                    mb = ((PMessageDescriptor) field.getDescriptor()).builder();
                                }
                                builder.set(field.getId(), parseMessage(tokenizer, (PMessageDescriptor) field.getDescriptor(), mb));
                                break;
                            }
                            case MAP: {
                                PMap<Object, Object> md = (PMap<Object, Object>) field.getDescriptor();
                                Map<Object,Object> map = mutableMap(md, builder.build().get(field.getId()));
                                builder.set(field.getId(), parseMapValue(token, tokenizer, (PMap<Object, Object>) field.getDescriptor(), map));
                                break;
                            }
                            default:
                                builder.set(field.getId(), parseValue(token, tokenizer, field.getDescriptor(), false));
                                break;
                        }
                    }
                }
            }
            sep = tokenizer.expectSymbol("Message end or sep", JsonToken.kListSep, JsonToken.kMapEnd);
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private Object parseValue(JsonToken token, JsonTokenizer tokenizer, PDescriptor descriptor, boolean valueRequired)
            throws IOException, JsonException {
        if (token.isNull() && !valueRequired) {
            return null;
        }

        switch (descriptor.getType()) {
            case VOID:
            case BOOL:
                if (token.isBoolean()) {
                    return token.booleanValue();
                } else {
                    throw new JsonException("Unexpected boolean value", tokenizer, token);
                }
            case BYTE:
                if (token.isInteger()) {
                    return (byte) token.intValue();
                } else {
                    throw new JsonException("Unexpected integer value", tokenizer, token);
                }
            case I16:
                if (token.isInteger()) {
                    return (short) token.intValue();
                } else {
                    throw new JsonException("Unexpected integer value", tokenizer, token);
                }
            case I32:
                if (token.isInteger()) {
                    return token.intValue();
                } else {
                    throw new JsonException("Unexpected integer value", tokenizer, token);
                }
            case I64:
                if (token.isInteger()) {
                    return token.longValue();
                } else {
                    throw new JsonException("Unexpected integer value", tokenizer, token);
                }
            case DOUBLE:
                if (token.isNumber()) {
                    return token.doubleValue();
                } else {
                    throw new JsonException("Unexpected double value", tokenizer, token);
                }
            case STRING:
                if (token.isLiteral()) {
                    return token.decodeJsonLiteral();
                } else {
                    throw new JsonException("Unexpected string value", tokenizer, token);
                }
            case BINARY:
                if (token.isLiteral()) {
                    return Binary.fromBase64(token.rawJsonLiteral());
                } else {
                    throw new JsonException("Unexpected binary value", tokenizer, token);
                }
            case ENUM: {
                PEnumDescriptor ed = (PEnumDescriptor) descriptor;
                try {
                    if (token.isInteger()) {
                        return ed.valueForId(token.intValue());
                    } else if (token.isLiteral()) {
                        return ed.valueForName(token.rawJsonLiteral());
                    } else {
                        throw new JsonException("Unexpected enum value", tokenizer, token);
                    }
                } catch (IllegalArgumentException e) {
                    if (strict || valueRequired) {
                        throw new JsonException("Unknown enum value", tokenizer, token);
                    }
                    return null;
                }
            }
            case SET:
            case LIST:
                if (token.isSymbol(JsonToken.kListStart)) {
                    List<Object> out = new ArrayList<>();
                    PContainer pls = (PContainer) descriptor;
                    while (!token.isSymbol(JsonToken.kListEnd)) {
                        out.add(parseValue(tokenizer.expect("list value"), tokenizer, pls.itemDescriptor(), true));
                        token = tokenizer.expect("List end or sep");
                        if (!token.isSymbol(JsonToken.kListEnd) && !token.isSymbol(JsonToken.kListSep)) {
                            throw new JsonException("Expected list end or sep", tokenizer, token);
                        }
                    }
                    return out;
                } else {
                    throw new JsonException("Unexpected start of list type, expected '['", tokenizer, token);
                }
            case MAP:
                if (token.isSymbol(JsonToken.kMapStart)) {
                    PMap map = (PMap) descriptor;
                    return parseMapValue(token, tokenizer, map, mutableMap(map, null));
                } else {
                    throw new JsonException("Unexpected start of map type, expected '{'", tokenizer, token);
                }
            case MESSAGE: {
                if (!token.isSymbol(JsonToken.kMapStart)) {
                    throw new JsonException("Unexpected start of message type, expected '{'", tokenizer, token);
                }
                PMessageDescriptor md = (PMessageDescriptor) descriptor;
                return parseMessage(tokenizer, md, md.builder());
            }
            default:
                throw new IllegalStateException("Impossible!");
        }
    }

    private Map<Object,Object> mutableMap(PMap map, Map<Object,Object> parent) {
        Map<Object,Object> out;
        if (map.builder() instanceof PMap.SortedBuilder) {
            out = new TreeMap<>();
        } else {
            out = new HashMap<>();
        }
        if (parent != null) {
            out.putAll(parent);
        }
        return out;
    }

    private Map<Object,Object> parseMapValue(JsonToken token,
                                             JsonTokenizer tokenizer,
                                             PMap<Object,Object> descriptor,
                                             Map<Object,Object> map) throws IOException, JsonException {
        if (token.isSymbol(JsonToken.kMapStart)) {
            while (!token.isSymbol(JsonToken.kMapEnd)) {
                Object key = ProvidenceConfigUtil.asType(descriptor.keyDescriptor(), tokenizer.expectString("Map key").decodeJsonLiteral());
                tokenizer.expectSymbol("Map kv sep", JsonToken.kKeyValSep);
                Object value = parseValue(tokenizer.expect("map value"), tokenizer, descriptor.itemDescriptor(), false);
                if (value == null) {
                    map.remove(key);
                } else {
                    map.put(key, value);
                }
                token = tokenizer.expect("Map end or sep");
                if (!token.isSymbol(JsonToken.kMapEnd) && !token.isSymbol(JsonToken.kListSep)) {
                    throw new JsonException("Expected list end or sep", tokenizer, token);
                }
            }
            return map;
        } else {
            throw new JsonException("Unexpected start of map type, expected '{'", tokenizer, token);
        }
    }

    private void consumeIntermediateJsonValue(JsonTokenizer tokenizer) throws IOException, JsonException {
        JsonToken token = tokenizer.expect("Json value");
        if (token.isSymbol(JsonToken.kListStart)) {
            // parse list and all in it.
            while (!token.isSymbol(JsonToken.kListEnd)) {
                consumeIntermediateJsonValue(tokenizer);
                token = tokenizer.expect("List sep or end");
                if (!token.isSymbol(JsonToken.kListSep) && !token.isSymbol(JsonToken.kListEnd)) {
                    throw new JsonException("Expected list separator or end", tokenizer, token);
                }
            }
        } else if (token.isSymbol(JsonToken.kMapStart)) {
            // parse map and all in it.
            while (!token.isSymbol(JsonToken.kListEnd)) {
                tokenizer.expectString("Map key");
                tokenizer.expectSymbol("Map KV sep", JsonToken.kKeyValSep);

                consumeIntermediateJsonValue(tokenizer);
                token = tokenizer.expect("Map entry sep or end");
                if (!token.isSymbol(JsonToken.kListSep) && !token.isSymbol(JsonToken.kMapEnd)) {
                    throw new JsonException("Expected list separator or end", tokenizer, token);
                }
            }
        } else if (token.isSymbol()) {
            // not allowed.
            throw new JsonException("Unexpected symbol '" + token.asString() + "'", tokenizer, token);
        }
    }

    /**
     * Type registry for looking up the base config types.
     */
    private final TypeRegistry registry;

    /**
     * If config should be parsed strictly.
     */
    private final boolean strict;

    static final String REPLACE = "replace";
    static final String AT_RESOLVE = "@resolve:";
}
