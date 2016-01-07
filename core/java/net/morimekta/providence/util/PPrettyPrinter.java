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

import net.morimekta.providence.Binary;
import net.morimekta.providence.PEnumValue;
import net.morimekta.providence.PMessage;
import net.morimekta.providence.PUnion;
import net.morimekta.providence.descriptor.PContainer;
import net.morimekta.providence.descriptor.PDescriptor;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.providence.descriptor.PMap;
import net.morimekta.providence.descriptor.PStructDescriptor;
import net.morimekta.providence.util.io.IndentedPrintWriter;
import net.morimekta.providence.util.json.JsonException;
import net.morimekta.providence.util.json.JsonWriter;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;

/**
 * Pretty printer that can print message content for easily reading
 * and debugging. This is a write only format used in stringifying
 * messages.
 *
 * @author Stein Eldar Johnsen
 * @since 25.08.15
 */
public class PPrettyPrinter {
    private final static String INDENT  = "  ";
    private final static String SPACE   = " ";
    private final static String NEWLINE = "\n";
    private final static String SEP     = ",";

    private final String mIndent;
    private final String mSpace;
    private final String mNewline;
    private final String mSep;

    public PPrettyPrinter() {
        this(INDENT, SPACE, NEWLINE, SEP);
    }

    public PPrettyPrinter(String indent,
                          String space,
                          String newline) {
        this(indent, space, newline, SEP);
    }

    public PPrettyPrinter(String indent,
                          String space,
                          String newline,
                          String sep) {
        mIndent = indent;
        mSpace = space;
        mNewline = newline;
        mSep = sep;
    }

    public String format(PMessage<?> message) {
        StringWriter stringWriter = new StringWriter();
        IndentedPrintWriter builder = new IndentedPrintWriter(stringWriter, mIndent, mNewline);
        try {
            appendMessage(builder, message);
            builder.flush();
        } finally {
            builder.close();
        }
        return stringWriter.toString();
    }

    private void appendMessage(IndentedPrintWriter builder, PMessage<?> message) {
        PStructDescriptor<?,?> type = message.descriptor();

        builder.append("{")
               .begin();

        if (message instanceof PUnion) {
            PField<?> field = ((PUnion) message).unionField();
            if (field != null) {
                Object o = message.get(field.getKey());

                builder.appendln(field.getName())
                       .append(":")
                       .append(mSpace);
                appendTypedValue(builder, field.getDescriptor(), o);
            }
        } else {
            boolean first = true;
            for (PField<?> field : type.getFields()) {
                if (message.has(field.getKey())) {
                    if (first)
                        first = false;
                    else
                        builder.append(mSep);
                    Object o = message.get(field.getKey());

                    builder.appendln(field.getName())
                           .append(":")
                           .append(mSpace);
                    appendTypedValue(builder, field.getDescriptor(), o);
                }
            }
        }

        builder.end()
               .appendln("}");
    }

    private void appendTypedValue(IndentedPrintWriter writer, PDescriptor descriptor, Object o) {
        switch (descriptor.getType()) {
            case LIST:
            case SET:
                writer.append("[")
                      .begin();

                PContainer<?, ?> containerType = (PContainer<?, ?>) descriptor;
                Collection<?> collection = (Collection<?>) o;

                boolean first = true;
                for (Object i : collection) {
                    if (first) {
                        first = false;
                    } else {
                        writer.append(',');
                    }
                    writer.appendln();
                    appendTypedValue(writer, containerType.itemDescriptor(), i);
                }

                writer.end()
                      .appendln("]");
                break;
            case MAP:
                PMap<?, ?> mapType = (PMap<?, ?>) descriptor;

                Map<?, ?> map = (Map<?, ?>) o;

                writer.append("{")
                      .begin();

                first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        writer.append(',');
                    }
                    writer.appendln();
                    appendTypedValue(writer, mapType.keyDescriptor(), entry.getKey());
                    writer.append(":")
                          .append(mSpace);
                    appendTypedValue(writer, mapType.itemDescriptor(), entry.getValue());
                }

                writer.end()
                      .appendln("}");
                break;
            case MESSAGE:
                PMessage<?> message = (PMessage<?>) o;
                appendMessage(writer, message);
                break;
            default:
                appendPrimitive(writer, o);
                break;
        }
    }

    private void appendPrimitive(IndentedPrintWriter writer, Object o) {
        if (o instanceof PEnumValue) {
            writer.print(o.toString());
        } else if (o instanceof String) {
            JsonWriter jw = new JsonWriter(writer);
            try {
                jw.value(o);
                jw.flush();
            } catch (JsonException e) {}
        } else if (o instanceof Binary) {
            writer.format("b64(%s)", ((Binary) o).toBase64());
        } else if (o instanceof Boolean) {
            writer.print(((Boolean) o).booleanValue());
        } else if (o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long) {
            writer.print(o.toString());
        } else if (o instanceof Double) {
            Double d = (Double) o;
            if (d == ((double) d.longValue())) {
                // actually an integer or long value.
                writer.print(d.longValue());
            } else if (d > ((10 << 9) - 1) || (1 / d) > (10 << 6)) {
                // Scientific notation should be used.
                writer.print((new DecimalFormat("0.#########E0")).format(d.doubleValue()));
            } else {
                writer.print(d.doubleValue());
            }
        } else {
            throw new IllegalArgumentException("Unknown primitive type class " + o.getClass().getSimpleName());
        }
    }
}
