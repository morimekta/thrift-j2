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

import net.morimekta.providence.PEnumValue;
import net.morimekta.providence.PMessage;
import net.morimekta.providence.PMessageBuilder;
import net.morimekta.providence.PType;
import net.morimekta.providence.config.ProvidenceConfigException;
import net.morimekta.providence.config.impl.ProvidenceConfigUtil.Stage;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.providence.descriptor.PMap;
import net.morimekta.providence.descriptor.PMessageDescriptor;
import net.morimekta.providence.serializer.pretty.Token;
import net.morimekta.providence.serializer.pretty.Tokenizer;
import net.morimekta.providence.serializer.pretty.TokenizerException;
import net.morimekta.providence.util.TypeRegistry;
import net.morimekta.util.Binary;
import net.morimekta.util.Pair;
import net.morimekta.util.io.Utf8StreamReader;
import net.morimekta.util.json.JsonWriter;
import net.morimekta.util.json.PrettyJsonWriter;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static net.morimekta.providence.config.impl.ProvidenceConfigParser.resolve;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.AS;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.DEF;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.DEFINE_REFERENCE;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.INCLUDE;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.RESERVED_WORDS;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.UNDEFINED;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.canonicalFileLocation;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.consumeValue;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.nextNotLineSep;
import static net.morimekta.providence.config.impl.ProvidenceConfigUtil.resolveFile;

/**
 * This parser parses config files. The class in itself should be stateless, so
 * can safely be used in multiple threads safely. This is a utility class created
 * in order to simplify testing.
 */
public class IntermediateConfigConverter {
    /**
     * Create a providence config parser instance.
     *
     * @param registry Type registry.
     * @param strict If config should be parsed and handled strictly.
     */
    public IntermediateConfigConverter(TypeRegistry registry, boolean strict) {
        this.registry = registry;
        this.parser = new ProvidenceConfigParser(registry, strict);
        this.strict = strict;
    }

    /**
     * Parse a providence config into a message.
     *
     * @param configFile The config file to be parsed.
     * @param intermediateFile File to write output to.
     * @throws ProvidenceConfigException If parsing failed.
     */
    public void convertConfig(@Nonnull Path configFile,
                              @Nonnull Path intermediateFile) throws ProvidenceConfigException {
        try {
            intermediateFile = canonicalFileLocation(intermediateFile);
        } catch (IOException e) {
            throw new ProvidenceConfigException(e, "Unable to resolve config file " + configFile).setFile(configFile.getFileName()
                                                                                                                    .toString());
        }

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(intermediateFile.toFile()))) {
            convertConfig(configFile, out);
        } catch (IOException e) {
            if (e instanceof ProvidenceConfigException) {
                throw (ProvidenceConfigException) e;
            }
            throw new ProvidenceConfigException(e, e.getMessage())
                    .setFile(configFile.getFileName().toString());
        }
    }

    /**
     * Parse a providence config into a message.
     *
     * @param configFile The config file to be parsed.
     * @param out Stream to write output to.
     * @throws ProvidenceConfigException If parsing failed.
     */
    public void convertConfig(@Nonnull Path configFile,
                              @Nonnull OutputStream out) throws ProvidenceConfigException {
        try {
            configFile = canonicalFileLocation(configFile);
        } catch (IOException e) {
            throw new ProvidenceConfigException(e, "Unable to resolve config file " + configFile)
                    .setFile(configFile.getFileName().toString());
        }

        try {
            Tokenizer tokenizer;
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(configFile.toFile()))) {
                // Non-enclosed content, meaning we should read the whole file immediately.
                tokenizer = new Tokenizer(new Utf8StreamReader(in), Tokenizer.DEFAULT_BUFFER_SIZE, true);
            }

            JsonWriter writer = new PrettyJsonWriter(out);

            convertConfigInternal(configFile, tokenizer, writer);

            writer.flush();
            out.write('\n');  // a final newline at end of file.
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
    private void convertConfigInternal(Path configFile, Tokenizer tokenizer, JsonWriter writer) throws IOException {
        ProvidenceConfigContext context = new ProvidenceConfigContext();

        Stage lastStage = Stage.INCLUDES;
        Token token = tokenizer.peek();
        while (token != null) {
            tokenizer.next();

            if (lastStage == Stage.MESSAGE) {
                throw new TokenizerException(token, "Unexpected token '" + token.asString() + "', expected end of file.")
                        .setLine(tokenizer.getLine());
            } else if (INCLUDE.equals(token.asString())) {
                // if include && stage == INCLUDES --> INCLUDES
                if (lastStage != Stage.INCLUDES) {
                    throw new TokenizerException(token, "Include added after defines or message. Only one def block allowed.")
                            .setLine(tokenizer.getLine());
                }
                token = tokenizer.expectLiteral("file to be included");
                String includedFilePath = token.decodeLiteral(strict);
                PMessage included;
                Path includedFile;
                try {
                    includedFile = resolveFile(configFile, includedFilePath);
                    Pair<PMessage, Set<String>> tmp = parser.parseConfigRecursively(includedFile, null, new String[]{configFile.toString()});
                    if (tmp != null) {
                        included = tmp.first;
                    } else {
                        included = null;
                    }
                } catch (FileNotFoundException e) {
                    throw new TokenizerException(token, "Included file \"%s\" not found.", includedFilePath)
                            .setLine(tokenizer.getLine());
                }
                token = tokenizer.expectIdentifier("the token 'as'");
                if (!AS.equals(token.asString())) {
                    throw new TokenizerException(token, "Expected token 'as' after included file \"%s\".", includedFilePath)
                            .setLine(tokenizer.getLine());
                }
                token = tokenizer.expectIdentifier("Include alias");
                String alias = token.asString();
                if (RESERVED_WORDS.contains(alias)) {
                    throw new TokenizerException(token, "Alias \"%s\" is a reserved word.", alias)
                            .setLine(tokenizer.getLine());
                }
                if (context.containsReference(alias)) {
                    throw new TokenizerException(token, "Alias \"%s\" is already used.", alias)
                            .setLine(tokenizer.getLine());
                }
                context.setInclude(alias, included);
            } else if (DEF.equals(token.asString())) {
                // if params && stage == DEF --> DEF
                lastStage = Stage.DEFINES;
                parser.parseDefinitions(context, tokenizer);
            } else if (token.isQualifiedIdentifier()) {
                // if a.b (type identifier) --> MESSAGE
                lastStage = Stage.MESSAGE;
                PMessageDescriptor descriptor;
                try {
                    descriptor = registry.getMessageType(token.asString());
                } catch (IllegalArgumentException e) {
                    // Unknown declared type. Fail if:
                    // - strict mode, all files must be of known types.
                    // - top of the stack. This is the config requested by the user. It should fail
                    //   even in non-strict mode.
                    throw new TokenizerException(token, "Unknown declared type: %s", token.asString()).setLine(
                            tokenizer.getLine());
                }
                convertConfigMessage(tokenizer, context, descriptor, writer, configFile);
            } else {
                throw new TokenizerException(token,
                                             "Unexpected token '" + token.asString() +
                                             "'. Expected include, defines or message type")
                        .setLine(tokenizer.getLine());
            }

            token = tokenizer.peek();
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends PMessage<M, F>, F extends PField> void convertConfigMessage(Tokenizer tokenizer,
                                                                                   ProvidenceConfigContext context,
                                                                                   PMessageDescriptor<M,F> descriptor,
                                                                                   JsonWriter writer,
                                                                                   Path file)
            throws IOException {
        if (tokenizer.expectSymbol("extension marker", Token.kKeyValueSep, Token.kMessageStart) == Token.kKeyValueSep) {
            Token token = tokenizer.expect("extension object");

            throw new TokenizerException(token, "Config in '" + file.getFileName().toString() + "' has both defined parent and inherits from")
                    .setLine(tokenizer.getLine())
                    .setFile(file.getFileName().toString());
        }

        writer.object();
        writer.key(descriptor.getQualifiedName());

        convertMessage(tokenizer, context, descriptor, writer);

        writer.endObject();
    }

    @SuppressWarnings("unchecked")
    private <M extends PMessage<M, F>, F extends PField>
    void convertMessage(@Nonnull Tokenizer tokenizer,
                        @Nonnull ProvidenceConfigContext context,
                        @Nonnull PMessageDescriptor<M, F> descriptor,
                        @Nonnull JsonWriter writer)
            throws IOException {
        writer.object();
        Token token = tokenizer.expect("object end or field");
        while (!token.isSymbol(Token.kMessageEnd)) {
            if (!token.isIdentifier()) {
                throw new TokenizerException(token, "Invalid field name: " + token.asString())
                        .setLine(tokenizer.getLine());
            }

            F field = descriptor.findFieldByName(token.asString());
            if (field == null) {
                if (strict) {
                    throw new TokenizerException("No such field " + token.asString() + " in " + descriptor.getQualifiedName())
                            .setLine(tokenizer.getLine());
                } else {
                    token = tokenizer.expect("field value sep, message start or reference start");
                    if (token.isSymbol(DEFINE_REFERENCE)) {
                        context.setReference(
                                context.initReference(tokenizer.expectIdentifier("reference name"), tokenizer),
                                null);
                        // Ignore reference.
                        token = tokenizer.expect("field value sep or message start");
                    }
                    if (token.isSymbol(Token.kFieldValueSep)) {
                        token = tokenizer.expect("value declaration");
                    } else if (!token.isSymbol(Token.kMessageStart)) {
                        throw new TokenizerException(token, "Expected field-value separator or inherited message")
                                .setLine(tokenizer.getLine());
                    }
                    // Non-strict will just consume unknown fields, this way
                    // we can be forward-compatible when reading config.
                    consumeValue(context, tokenizer, token);
                    token = nextNotLineSep(tokenizer, "field or message end");
                    continue;
                }
            }

            if (field.getType() == PType.MESSAGE) {
                // go recursive with optional
                String reference = null;
                char symbol = tokenizer.expectSymbol(
                        "Message assigner or start",
                        Token.kFieldValueSep,
                        Token.kMessageStart,
                        DEFINE_REFERENCE);
                if (symbol == DEFINE_REFERENCE) {
                    Token ref = tokenizer.expectIdentifier("reference name");
                    if (strict) {
                        throw tokenizer.failure(ref, "Reusable objects are not allowed in strict mode.");
                    }
                    reference = context.initReference(ref, tokenizer);
                    symbol = tokenizer.expectSymbol("Message assigner or start after " + reference, Token.kFieldValueSep, Token.kMessageStart);
                }

                PMessageBuilder bld;
                if (symbol == Token.kFieldValueSep) {
                    token = tokenizer.expect("reference or message start");
                    if (UNDEFINED.equals(token.asString())) {
                        // unset.
                        writer.key(field.getName());
                        writer.value((String) null);

                        context.setReference(reference, null);

                        // special casing this, as we don't want to duplicate the parse line below.
                        token = nextNotLineSep(tokenizer, "field or message end");
                        continue;
                    }

                    writer.key(IntermediateConfigParser.AT_RESOLVE + field.getName());
                    writer.value(IntermediateConfigParser.REPLACE);

                    // overwrite with new.
                    bld = ((PMessageDescriptor) field.getDescriptor()).builder();
                    if (token.isReferenceIdentifier()) {
                        // Inherit from reference.
                        try {
                            PMessage ref = resolve(context, token, tokenizer, field.getDescriptor());
                            if (ref != null) {
                                bld.merge(ref);
                            } else if (tokenizer.peek().isSymbol(Token.kMessageStart)) {
                                throw new TokenizerException(token, "Inherit from unknown reference %s", token.asString()).setLine(tokenizer.getLine());
                            } else if (strict) {
                                throw new TokenizerException(token, "Unknown reference %s", token.asString()).setLine(
                                        tokenizer.getLine());
                            }
                        } catch (ProvidenceConfigException e) {
                            throw new TokenizerException(token, "Unknown inherited reference '%s'", token.asString())
                                    .setLine(tokenizer.getLine());
                        }

                        token = tokenizer.expect("after message reference");
                        // if the following symbol is *not* message start,
                        // we assume a new field or end of current message.
                        if (!token.isSymbol(Token.kMessageStart)) {
                            writer.key(field.getName());
                            writeValue(bld.build(), writer);
                            continue;
                        }

                        Object msg = parser.parseMessage(tokenizer, context, bld);
                        writer.key(field.getName());
                        writeValue(msg, writer);
                        token = nextNotLineSep(tokenizer, "field or message end");
                        continue;
                    } else if (!token.isSymbol(Token.kMessageStart)) {
                        throw new TokenizerException(token,
                                                     "Unexpected token " + token.asString() +
                                                     ", expected message start").setLine(tokenizer.getLine());
                    }
                }
                writer.key(field.getName());
                convertMessage(tokenizer, context, (PMessageDescriptor) field.getDescriptor(), writer);
            } else if (field.getType() == PType.MAP) {
                // maps can be extended the same way as
                token = tokenizer.expect("field sep or value start");
                Map baseValue = null;
                if (token.isSymbol(DEFINE_REFERENCE)) {
                    Token ref = tokenizer.expectIdentifier("reference name");
                    throw tokenizer.failure(ref, "Reusable objects are not allowed in strict mode.");
                }

                if (token.isSymbol(Token.kFieldValueSep)) {
                    writer.key(IntermediateConfigParser.AT_RESOLVE + field.getName());
                    writer.value(IntermediateConfigParser.REPLACE);

                    token = tokenizer.expect("field id or start");
                    if (UNDEFINED.equals(token.asString())) {

                        writer.key(field.getName());
                        writer.value((String) null);

                        token = nextNotLineSep(tokenizer, "field or message end");
                        continue;
                    } else if (token.isReferenceIdentifier()) {
                        try {
                            baseValue = resolve(context, token, tokenizer, field.getDescriptor());
                        } catch (ProvidenceConfigException e) {
                            throw new TokenizerException(token, e.getMessage())
                                    .setLine(tokenizer.getLine());
                        }

                        token = tokenizer.expect("map start or next field");
                        if (!token.isSymbol(Token.kMessageStart)) {

                            writer.key(field.getName());
                            writeValue(baseValue, writer);

                            token = nextNotLineSep(tokenizer, "field or message end");
                            continue;
                        }
                    }
                    if (baseValue == null) {
                        baseValue = new LinkedHashMap();
                    }

                    Map map = parser.parseMapValue(tokenizer, context, (PMap) field.getDescriptor(), baseValue);
                    writer.key(field.getName());
                    writeValue(map, writer);
                } else {
                    if (!token.isSymbol(Token.kMessageStart)) {
                        throw new TokenizerException(token, "Expected map start, but got '%s'", token.asString()).setLine(tokenizer.getLine());
                    }

                    writer.key(field.getName());
                    convertMap(tokenizer, context, (PMap) field.getDescriptor(), writer);
                }
            } else {
                String reference = null;
                // Simple fields *must* have the '=' separation, may have '&' reference.
                if (tokenizer.expectSymbol("field value sep", Token.kFieldValueSep, DEFINE_REFERENCE) ==
                    DEFINE_REFERENCE) {
                    Token ref = tokenizer.expectIdentifier("reference name");
                    if (strict) {
                        throw tokenizer.failure(ref, "Reusable objects are not allowed in strict mode.");
                    }
                    reference = context.initReference(ref, tokenizer);
                    tokenizer.expectSymbol("field value sep", Token.kFieldValueSep);
                }
                token = tokenizer.expect("field value");
                if (UNDEFINED.equals(token.asString())) {

                    writer.key(field.getName());
                    writer.value((String) null);

                    context.setReference(reference, null);
                } else {
                    Object value = parser.parseFieldValue(token, tokenizer, context, field.getDescriptor(), strict);

                    writer.key(field.getName());
                    writeValue(value, writer);
                }
            }

            token = nextNotLineSep(tokenizer, "field or message end");
        }

        writer.endObject();
    }

    @SuppressWarnings("unchecked")
    private void convertMap(Tokenizer tokenizer,
                            ProvidenceConfigContext context,
                            PMap descriptor,
                            JsonWriter writer) throws IOException {
        Token next = tokenizer.expect("map key or end");
        writer.object();
        while (!next.isSymbol(Token.kMessageEnd)) {
            Object key = parser.parseFieldValue(next, tokenizer, context, descriptor.keyDescriptor(), strict);
            writeMapKey(key, writer);

            tokenizer.expectSymbol("map key value sep", Token.kKeyValueSep);
            next = tokenizer.expect("map value");

            if (UNDEFINED.equals(next.asString())) {
                writer.value((String) null);
            } else {
                Object value = parser.parseFieldValue(next, tokenizer, context, descriptor.itemDescriptor(), strict);
                writeValue(value, writer);
            }
            // maps do *not* require separator, but allows ',' separator, and separator after last.
            next = tokenizer.expect("map key, end or sep");
            if (next.isSymbol(Token.kLineSep1)) {
                next = tokenizer.expect("map key or end");
            }
        }
        writer.endObject();
    }

    private void writeMapKey(Object key, JsonWriter writer) throws ProvidenceConfigException {
        if (key == null) {
            throw new ProvidenceConfigException("foo");
        } else if (key instanceof Boolean) {
            writer.key((Boolean) key);
        } else if (key instanceof Double) {
            writer.key((Double) key);
        } else if (key instanceof Number) {
            writer.key(((Number) key).longValue());
        } else if (key instanceof CharSequence) {
            writer.key((CharSequence) key);
        } else if (key instanceof Binary) {
            writer.key((Binary) key);
        } else if (key instanceof PEnumValue) {
            writer.key(((PEnumValue) key).asInteger());
        } else {
            throw new ProvidenceConfigException("foo");
        }

    }

    private void writeValue(Object value, JsonWriter writer) throws ProvidenceConfigException {
        if (value == null) {
            writer.value((String) null);
        } else if (value instanceof Boolean) {
            writer.value((Boolean) value);
        } else if (value instanceof Double) {
            writer.value((Double) value);
        } else if (value instanceof Number) {
            writer.value(((Number) value).longValue());
        } else if (value instanceof CharSequence) {
            writer.value((CharSequence) value);
        } else if (value instanceof Binary) {
            writer.value((Binary) value);
        } else if (value instanceof PEnumValue) {
            writer.value(((PEnumValue) value).asInteger());
        } else if (value instanceof Collection) {
            writer.array();
            for (Object item : (Collection) value) {
                writeValue(item, writer);
            }
            writer.endArray();
        } else if (value instanceof Map) {
            writer.object();
            for (Map.Entry entry : ((Map<?,?>) value).entrySet()) {
                writeMapKey(entry.getKey(), writer);
                writeValue(entry.getValue(), writer);
            }
            writer.endObject();
        } else if (value instanceof PMessage) {
            writer.object();
            PMessage message = (PMessage) value;
            for (PField field : message.descriptor().getFields()) {
                if (message.has(field.getId())) {
                    writer.key(field.getName());
                    writeValue(message.get(field.getId()), writer);
                }
            }
            writer.endObject();
        } else {
            throw new ProvidenceConfigException("foo");
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
    private final ProvidenceConfigParser parser;
}
