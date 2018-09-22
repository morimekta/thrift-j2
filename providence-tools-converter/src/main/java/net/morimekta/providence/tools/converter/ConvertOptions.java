/*
 * Copyright (c) 2016, Providence Authors
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

package net.morimekta.providence.tools.converter;

import net.morimekta.console.args.Argument;
import net.morimekta.console.args.ArgumentException;
import net.morimekta.console.args.ArgumentParser;
import net.morimekta.console.args.Flag;
import net.morimekta.console.args.Option;
import net.morimekta.console.util.STTY;
import net.morimekta.providence.PMessage;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.providence.descriptor.PMessageDescriptor;
import net.morimekta.providence.descriptor.PService;
import net.morimekta.providence.mio.MessageReader;
import net.morimekta.providence.mio.MessageWriter;
import net.morimekta.providence.reflect.TypeLoader;
import net.morimekta.providence.reflect.contained.CProgram;
import net.morimekta.providence.reflect.parser.ParseException;
import net.morimekta.providence.reflect.util.ProgramRegistry;
import net.morimekta.providence.reflect.util.ReflectionUtils;
import net.morimekta.providence.serializer.Serializer;
import net.morimekta.providence.tools.common.CommonOptions;
import net.morimekta.providence.tools.common.formats.ConvertStream;
import net.morimekta.providence.tools.common.formats.ConvertStreamParser;
import net.morimekta.providence.tools.common.formats.Format;
import net.morimekta.providence.tools.common.formats.FormatUtils;
import net.morimekta.util.Strings;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.morimekta.console.util.Parser.dir;

/**
 * Options used by the providence converter.
 */
@SuppressWarnings("all")
public class ConvertOptions extends CommonOptions {
    @Nonnull
    protected List<File>    includes  = new ArrayList<>();
    @Nonnull
    protected ConvertStream in        = new ConvertStream(Format.json);
    @Nonnull
    protected ConvertStream out       = new ConvertStream(Format.pretty_json);
    protected boolean       strict    = false;
    protected boolean       listTypes = false;
    protected String        type      = null;

    public ConvertOptions(STTY tty) {
        super(tty);
    }

    @Override
    public ArgumentParser getArgumentParser(String prog, String description) throws IOException {
        ArgumentParser parser = super.getArgumentParser(prog, description);

        parser.add(new Option("--include", "I", "dir", "Include from directories.",
                              dir(this::addInclude), "${PWD}", true, false, false));
        parser.add(new Option("--in", "i", "spec", "Input specification",
                              new ConvertStreamParser(in).andApply(this::setIn), in.toString()));
        parser.add(new Option("--out", "o", "spec", "Output specification",
                              new ConvertStreamParser(out).andApply(this::setOut), out.toString()));
        parser.add(new Flag("--strict", "S", "Read incoming messages strictly.",
                            this::setStrict));
        parser.add(new Flag("--list-types", "L", "List the parsed types based on the input files",
                            this::setListTypes));
        parser.add(new Argument("type", "Qualified identifier name from definitions to use for parsing source file.",
                                this::setType));

        return parser;
    }

    private void addInclude(File include) {
        this.includes.add(include);
    }

    private void setIn(ConvertStream in) {
        this.in = in;
    }

    private void setOut(ConvertStream out) {
        this.out = out;
    }

    private void setListTypes(Boolean b) {
        this.listTypes = b;
    }

    private void setStrict(boolean strict) {
        this.strict = strict;
    }

    private void setType(String type) {
        this.type = type;
    }

    private Serializer getSerializer(Format format) {
        return format.createSerializer(strict);
    }

    public ProgramRegistry getProgramRegistry() throws IOException {
        Map<String, File> includeMap = FormatUtils.getIncludeMap(getRc(), includes);
        if (type.isEmpty()) {
            throw new ArgumentException("Missing input type name");
        }

        Set<File> rootSet = new TreeSet<File>();
        for (File file : includeMap.values()) {
            rootSet.add(file.getParentFile());
        }

        String programName = type.substring(0, type.lastIndexOf("."));
        programName = programName.replaceAll("[-.]", "_");

        TypeLoader loader = new TypeLoader(rootSet);

        try {
            if (!includeMap.containsKey(programName)) {
                throw new ArgumentException("No program " + programName + " found in include path.\n" +
                                            "Found: " + Strings.join(", ", new TreeSet<Object>(includeMap.keySet())));
            }

            loader.load(includeMap.get(programName));
        } catch (IOException e) {
            throw new ArgumentException(e.getLocalizedMessage());
        }

        return loader.getProgramRegistry();
    }

    @SuppressWarnings("unchecked")
    public <Message extends PMessage<Message, Field>, Field extends PField>
    PMessageDescriptor<Message, Field> getDefinition() throws ParseException, IOException {
        ProgramRegistry registry = getProgramRegistry();
        try {
            return (PMessageDescriptor) registry.getDeclaredType(type);
        } catch (IllegalArgumentException e) {
            try {
                registry.getService(type);
                return null;
            } catch (IllegalArgumentException e2) {
                e.addSuppressed(e2);
                throw e;
            }
        }
    }

    public PService getServiceDefinition() throws ParseException, IOException {
        ProgramRegistry registry = getProgramRegistry();
        try {
            return registry.getService(type, null);
        } catch (Exception e) {
            String programName = type.substring(0, type.lastIndexOf("."));
            programName = programName.replaceAll("[-.]", "_");
            Map<String, File> includeMap = FormatUtils.getIncludeMap(getRc(), includes);
            String filePath = includeMap.get(programName).toString();

            CProgram document = registry.registryForPath(filePath).getProgram();
            Set<String> services = new TreeSet<>(
                    document.getServices()
                            .stream().map(s -> s.getQualifiedName())
                            .collect(Collectors.toSet()));

            throw new ArgumentException(
                    "Unknown service %s in %s.\n" +
                    "Found %s", type, ReflectionUtils.programNameFromPath(type),
                    services.size() == 0 ? "none" : Strings.join(", ", services));
        }
    }

    public <Message extends PMessage<Message, Field>, Field extends PField>
    Collector<Message, ?, Integer> getOutput()
            throws IOException {
        return FormatUtils.getOutput(out, strict);
    }

    public <Message extends PMessage<Message, Field>, Field extends PField>
    Stream<Message> getInput() throws ParseException, IOException {
        PMessageDescriptor<Message, Field> descriptor = getDefinition();
        return FormatUtils.getInput(descriptor, in, strict);
    }

    public MessageReader getServiceInput() throws IOException {
        return FormatUtils.getServiceInput(in, strict);
    }

    public MessageWriter getServiceOutput()
            throws IOException {
        return FormatUtils.getServiceOutput(out, strict);
    }

}
