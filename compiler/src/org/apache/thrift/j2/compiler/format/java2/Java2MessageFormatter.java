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

package org.apache.thrift.j2.compiler.format.java2;

import java.io.IOException;

import org.apache.thrift.j2.TException;
import org.apache.thrift.j2.TMessage;
import org.apache.thrift.j2.TMessageBuilder;
import org.apache.thrift.j2.TMessageBuilderFactory;
import org.apache.thrift.j2.TMessageVariant;
import org.apache.thrift.j2.compiler.generator.GeneratorException;
import org.apache.thrift.j2.descriptor.TContainer;
import org.apache.thrift.j2.descriptor.TDefaultValueProvider;
import org.apache.thrift.j2.descriptor.TExceptionDescriptorProvider;
import org.apache.thrift.j2.descriptor.TField;
import org.apache.thrift.j2.descriptor.TList;
import org.apache.thrift.j2.descriptor.TMap;
import org.apache.thrift.j2.descriptor.TPrimitive;
import org.apache.thrift.j2.descriptor.TSet;
import org.apache.thrift.j2.descriptor.TStructDescriptor;
import org.apache.thrift.j2.descriptor.TStructDescriptorProvider;
import org.apache.thrift.j2.descriptor.TUnionDescriptor;
import org.apache.thrift.j2.descriptor.TUnionDescriptorProvider;
import org.apache.thrift.j2.util.TTypeUtils;
import org.apache.thrift.j2.util.io.IndentedPrintWriter;
import org.apache.thrift.j2.descriptor.TDescriptor;
import org.apache.thrift.j2.descriptor.TExceptionDescriptor;
import org.json.JSONObject;

import static org.apache.thrift.j2.util.TStringUtils.camelCase;

/**
 * @author Stein Eldar Johnsen <steineldar@zedge.net>
 * @since 20.09.15
 */
public class Java2MessageFormatter {
    public static final String DBL_INDENT =
            IndentedPrintWriter.INDENT +
            IndentedPrintWriter.INDENT;

    private final Java2TypeHelper mTypeHelper;
    private final boolean         mAndroid;

    public Java2MessageFormatter(Java2TypeHelper helper,
                                 boolean android) {
        mTypeHelper = helper;
        mAndroid = android;
    }

    public void format(IndentedPrintWriter writer, TStructDescriptor<?> type) throws GeneratorException, IOException {
        appendFileHeader(writer, type);

        if (type.getComment() != null) {
            Java2Utils.appendBlockComment(writer, type.getComment());
        }

        appendClassDefinitionStart(writer, type);

        appendFieldDefaultConstants(writer, type);
        appendFieldDeclarations(writer, type);

        appendConstructor(writer, type);

        appendFieldGetters(writer, type);
        appendInheritedGetter_has(writer, type);
        appendInheritedGetter_num(writer, type);
        appendInheritedGetter_get(writer, type);

        appendObjectCompact(writer, type);
        appendObjectEquals(writer, type);
        appendObjectHashCode(writer, type);
        appendObjectToString(writer);

        appendIsValid(writer, type);

        appendDescriptor(writer, type);

        if (mAndroid) {
            appendParcelable(writer, type);
        }

        appendBuilder(writer, type);
        //  - mutate
        //  - Builder class

        appendClassDefinitionEnd(writer);
    }

    private void appendObjectCompact(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        if (type.isCompactible()) {
            writer.appendln("@Override")
                  .appendln("public boolean compact() {")
                  .begin()
                  .appendln("boolean missing = false;");

            for (TField<?> field : type.getFields()) {
                writer.formatln("if (%s()) {", camelCase("has", field.getName()))
                      .begin()
                      .appendln("if (missing) return false;")
                      .end()
                      .appendln("} else {")
                      .begin()
                      .appendln("missing = true;")
                      .end()
                      .appendln('}');
            }

            writer.appendln("return true;")
                  .end()
                  .appendln('}')
                  .newline();
        } else {
            writer.appendln("@Override")
                  .appendln("public boolean compact() {")
                  .begin()
                  .appendln("return false;")
                  .end()
                  .appendln('}')
                  .newline();
        }
    }

    private void appendParcelable(IndentedPrintWriter writer, TStructDescriptor<?> type) throws GeneratorException {
        writer.appendln("@Override")
              .appendln("public int describeContents() {")
              .begin()
              .appendln("return 0;")
              .end()
              .appendln('}')
              .newline();

        writer.appendln("@Override")
              .appendln("public void writeToParcel(Parcel dest, int flags) {")
              .begin();
        for (TField<?> field : type.getFields()) {
            String fName = camelCase("m", field.getName());
            switch (field.descriptor().getType()) {
                case LIST:
                case SET:
                case MAP:
                    writer.formatln("if (%s() > 0) {", camelCase("num", field.getName()));
                    break;
                default:
                    writer.formatln("if (%s()) {", camelCase("has", field.getName()));
                    break;
            }
            writer.begin()
                  .formatln("dest.writeInt(%d);", field.getKey());
            switch (field.descriptor().getType()) {
                case BOOL:
                    writer.formatln("dest.writeByte(%s ? (byte) 1 : (byte) 0);", fName);
                    break;
                case BYTE:
                    writer.formatln("dest.writeByte(%s);", fName);
                    break;
                case I16:
                case I32:
                    writer.formatln("dest.writeInt(%s);", fName);
                    break;
                case I64:
                    writer.formatln("dest.writeLong(%s);", fName);
                    break;
                case DOUBLE:
                    writer.formatln("dest.writeDouble(%s);", fName);
                    break;
                case STRING:
                    writer.formatln("dest.writeString(%s);", fName);
                    break;
                case BINARY:
                    writer.formatln("dest.writeInt(%s.length);", fName);
                    writer.formatln("dest.writeByteArray(%s);", fName);
                    break;
                case ENUM:
                    writer.formatln("dest.writeInt(%s.getValue());", fName);
                    break;
                case MESSAGE:
                    writer.formatln("dest.writeParcelable(%s, 0);", fName);
                    break;
                case SET:
                case LIST:
                    TContainer<?, ?> cType = (TContainer<?, ?>) field.descriptor();
                    String iTypeName = mTypeHelper.getSimpleClassName(cType.itemDescriptor());
                    switch (cType.itemDescriptor().getType()) {
                        case LIST:
                        case SET:
                        case MAP:
                            throw new GeneratorException("Nested containers not allowed with --android");
                        case MESSAGE:
                            writer.formatln("dest.writeParcelableArray(%s.toArray(new %s[%s.size()]), %s.size());",
                                            fName, iTypeName, fName, fName);
                            break;
                        case BINARY:
                            writer.formatln("dest.writeInt(%s.size());", fName)
                                  .formatln("for (byte[] item : %s) {", fName)
                                  .begin()
                                  .formatln("dest.writeInt(item.length);", fName)
                                  .formatln("dest.writeByteArray(item);", fName)
                                  .end()
                                  .appendln('}');
                        default:
                            writer.formatln("dest.writeArray(%s.toArray());", fName);
                    }
                    break;
                case MAP:
                    TMap<?, ?> mType = (TMap<?, ?>) field.descriptor();
                    String kTypeName = mTypeHelper.getSimpleClassName(mType.keyDescriptor());
                    iTypeName = mTypeHelper.getSimpleClassName(mType.itemDescriptor());
                    writer.formatln("dest.writeInt(%s.size());", fName);
                    switch (mType.keyDescriptor().getType()) {
                        case LIST:
                        case SET:
                        case MAP:
                            throw new GeneratorException("Containers not allowed in map key.");
                        case MESSAGE:
                            throw new GeneratorException("Messages not allowed for map key.");
                        case BINARY:
                            writer.formatln("for (byte[] item : %s.keySet()) {", fName)
                                  .begin()
                                  .appendln("dest.writeInt(item.length);")
                                  .appendln("dest.writeByteArray(item);")
                                  .end()
                                  .appendln('}');
                            break;
                        default:
                            writer.formatln("dest.writeArray(%s.keySet().toArray(new %s[%s.size()]));",
                                            fName, kTypeName, fName);
                            break;

                    }
                    switch (mType.itemDescriptor().getType()) {
                        case LIST:
                        case SET:
                        case MAP:
                            throw new GeneratorException("Nested containers not allowed with --android");
                        case MESSAGE:
                            writer.formatln("%s[] values = %s.values().toArray(new %s[%s.size()]);",
                                            iTypeName, fName, iTypeName, fName);
                            writer.appendln(
                                    "dest.writeParcelableArray(values, values.length);");
                            break;
                        case BINARY:
                            writer.formatln("for (byte[] item : %s.values()) {", fName)
                                  .begin()
                                  .appendln("dest.writeInt(item.length);")
                                  .appendln("dest.writeByteArray(item);")
                                  .end()
                                  .appendln('}');
                            break;
                        default:
                            writer.formatln("dest.writeArray(%s.values().toArray(new %s[%s.size()]));",
                                            fName, iTypeName, fName);
                            break;
                    }
                    break;
            }
            writer.end()
                  .appendln('}');
        }
        writer.appendln("dest.writeInt(0);")
              .end()
              .appendln('}')
              .newline();

        String simpleClass = mTypeHelper.getSimpleClassName(type);

        writer.formatln("public static final Parcelable.Creator<%s> CREATOR = new Parcelable.Creator<%s>() {",
                        simpleClass, simpleClass)
              .begin();

        writer.appendln("@Override")
              .formatln("public %s createFromParcel(Parcel source) {", simpleClass)
              .begin()
              .formatln("%s.Builder builder = new %s.Builder();", simpleClass, simpleClass)
              .appendln("loop: while (source.dataAvail() > 0) {")
              .begin()
              .appendln("int field = source.readInt();")
              .appendln("switch (field) {")
              .begin()
              .appendln("case 0: break loop;");

        for (TField<?> field : type.getFields()) {
            writer.formatln("case %d:", field.getKey()).begin();

            String setF = camelCase("set", field.getName());
            String addToF = camelCase("addTo", field.getName());
            String classF = mTypeHelper.getSimpleClassName(field.descriptor());

            switch (field.descriptor().getType()) {
                case BOOL:
                    writer.formatln("builder.%s(source.readByte() > 0);", setF);
                    break;
                case BYTE:
                    writer.formatln("builder.%s(source.readByte());", setF);
                    break;
                case I16:
                    writer.formatln("builder.%s((short)source.readInt());", setF);
                    break;
                case I32:
                    writer.formatln("builder.%s(source.readInt());", setF);
                    break;
                case I64:
                    writer.formatln("builder.%s(source.readLong());", setF);
                    break;
                case DOUBLE:
                    writer.formatln("builder.%s(source.readDouble());", setF);
                    break;
                case STRING:
                    writer.formatln("builder.%s(source.readString());", setF);
                    break;
                case BINARY:
                    writer.append(" {")
                          .begin()
                          .appendln("int len = source.readInt();")
                          .appendln("byte[] bytes = new byte[len];")
                          .formatln("source.readByteArray(bytes);")
                          .formatln("builder.%s(bytes);", setF)
                          .end()
                          .appendln('}');
                    break;
                case ENUM:
                    writer.formatln("builder.%s(%s.valueOf(source.readInt()));",
                                    setF, classF);
                    break;
                case MESSAGE:
                    writer.formatln("builder.%s((%s) source.readParcelable(%s.class.getClassLoader()));",
                                    setF, classF, classF);
                    break;
                case LIST:
                case SET:
                    TContainer<?, ?> cType = (TContainer<?, ?>) field.descriptor();
                    String cItemClass = mTypeHelper.getSimpleClassName(cType.itemDescriptor());
                    switch (cType.itemDescriptor().getType()) {
                        case LIST:
                        case SET:
                        case MAP:
                            throw new GeneratorException("Nested containers not allowed with --android");
                        case BINARY:
                            writer.append(" {")
                                  .begin()
                                  .formatln("int len = source.readInt();")
                                  .appendln("for (int i = 0; i < len; ++i) {")
                                  .begin()
                                  .appendln("int bl = source.readInt();")
                                  .appendln("byte[] bytes = new byte[bl];")
                                  .formatln("source.readByteArray(bytes);")
                                  .formatln("builder.%s(bytes);", addToF)
                                  .end()
                                  .appendln('}')
                                  .end()
                                  .appendln('}');
                            break;
                        case MESSAGE:
                            writer.formatln(
                                    "builder.%s((%s[]) source.readParcelableArray(%s.class.getClassLoader()));",
                                    addToF, cItemClass, cItemClass);
                            break;
                        default:
                            writer.formatln("builder.%s((%s[]) source.readArray(%s.class.getClassLoader()));",
                                            addToF, cItemClass, cItemClass);
                            break;
                    }
                    break;
                case MAP:
                    writer.append(" {")
                          .begin()
                          .appendln("int len = source.readInt();");
                    TMap<?, ?> mType = (TMap<?, ?>) field.descriptor();
                    String mkClass = mTypeHelper.getSimpleClassName(mType.keyDescriptor());
                    String miClass = mTypeHelper.getSimpleClassName(mType.itemDescriptor());

                    switch (mType.keyDescriptor().getType()) {
                        case MAP:
                        case SET:
                        case LIST:
                            throw new GeneratorException("Containers not allowed in map key.");
                        case MESSAGE:
                            throw new GeneratorException("Messages not allowed for map key.");
                        case BINARY:
                            writer.appendln("byte[][] keys = new byte[len][];")
                                  .appendln("for (int i = 0; i < len; ++i) {")
                                  .begin()
                                  .appendln("keys[i] = new byte[source.readInt()];")
                                  .formatln("source.readByteArray(keys[i]);")
                                  .end()
                                  .appendln('}');
                            break;
                        default:
                            writer.formatln("%s[] keys = (%s[]) source.readArray(%s.class.getClassLoader());",
                                            mkClass, mkClass, mkClass);
                            break;
                    }
                    switch (mType.itemDescriptor().getType()) {
                        case MAP:
                        case SET:
                        case LIST:
                            throw new GeneratorException("Nested containers not allowed with --android");
                        case BINARY:
                            writer.appendln("byte[][] values = new byte[len][];")
                                  .appendln("for (int i = 0; i < len; ++i) {")
                                  .begin()
                                  .appendln("values[i] = new byte[source.readInt()];")
                                  .formatln("source.readByteArray(values[i]);")
                                  .end()
                                  .appendln('}');
                            break;
                        case MESSAGE:
                            writer.formatln("%s[] values = (%s[]) source.readParcelableArray(%s.class.getClassLoader());",
                                            miClass, miClass, miClass);
                            break;
                        default:
                            writer.formatln("%s[] values = (%s[]) source.readArray(%s.class.getClassLoader());",
                                            miClass, miClass, miClass);
                            break;
                    }
                    writer.formatln("for (int i = 0; i < len; ++i) {")
                          .begin()
                          .formatln("builder.%s(keys[i], values[i]);", addToF)
                          .end()
                          .appendln('}')
                          .end()
                          .appendln('}');
                    break;
            }

            writer.appendln("break;").end();
        }

        writer.appendln("default: throw new IllegalArgumentException(\"Unknown field ID: \" + field);")
              .end()
              .appendln('}')
              .end()
              .appendln('}')
              .newline()
              .appendln("return builder.build();")
              .end()
              .appendln('}')
              .newline();

        writer.appendln("@Override")
                .formatln("public %s[] newArray(int size) {", simpleClass)
                .begin()
                .formatln("return new %s[size];", simpleClass)
                .end()
                .appendln('}');

        writer.end()
              .appendln("};")
              .newline();
    }

    private void appendBuilder(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        String simpleClass = mTypeHelper.getSimpleClassName(type);
        writer.appendln("@Override")
              .formatln("public %s.Builder mutate() {", simpleClass)
              .begin()
              .formatln("return new %s.Builder(this);", simpleClass)
              .end()
              .appendln('}')
              .newline();
        writer.formatln("public static %s.Builder builder() {", simpleClass)
              .begin()
              .formatln("return new %s.Builder();", simpleClass)
              .end()
              .appendln('}')
              .newline();

        writer.appendln("public static class Builder")
              .begin()
              .formatln("    extends TMessageBuilder<%s> {", simpleClass);
        for (TField<?> field : type.getFields()) {
            writer.formatln("private %s %s;",
                            mTypeHelper.getFieldType(field.descriptor()),
                            camelCase("m", field.getName()));
        }

        writer.newline()
              .appendln("public Builder() {")
              .begin();
        for (TField<?> field : type.getFields()) {
            switch (field.descriptor().getType()) {
                case MAP:
                case SET:
                case LIST:
                    writer.formatln("%s = new %s<>();",
                                    camelCase("m", field.getName()),
                                    mTypeHelper.getSimpleClassName(field.descriptor()));
                    break;
                default:
                    break;
            }
        }
        // Builder - default constructor
        writer.end()
              .appendln('}')
              .newline();

        writer.formatln("public Builder(%s base) {", simpleClass)
              .begin()
              .appendln("this();")
              .newline();
        for (TField<?> field : type.getFields()) {
            String fName = camelCase("m", field.getName());
            switch (field.descriptor().getType()) {
                case LIST:
                case SET:
                    writer.formatln("%s.addAll(base.%s);", fName, fName);
                    break;
                case MAP:
                    writer.formatln("%s.putAll(base.%s);", fName, fName);
                    break;
                default:
                    writer.formatln("%s = base.%s;", fName, fName);
                    break;
            }
        }
        // Builder - mutate constructor
        writer.end()
              .appendln('}')
              .newline();

        for (TField<?> field : type.getFields()) {
            String fName = camelCase("m", field.getName());
            String vType = mTypeHelper.getValueType(field.descriptor());
            switch (field.descriptor().getType()) {
                case MAP:
                    TMap<?, ?> mType = (TMap<?, ?>) field.descriptor();
                    String mkType = mTypeHelper.getSimpleClassName(mType.keyDescriptor());
                    String miType = mTypeHelper.getSimpleClassName(mType.itemDescriptor());
                    if (field.getComment() != null) {
                        Java2Utils.appendBlockComment(writer, field.getComment());
                    }
                    writer.formatln("public Builder %s(Map<%s,%s> value) {",
                                    camelCase("set", field.getName()),
                                    mkType, miType)
                          .begin()
                          .formatln("%s.clear();", fName)
                          .formatln("%s.putAll(value);", fName)
                          .appendln("return this;")
                          .end()
                          .appendln('}')
                          .newline();
                    if (field.getComment() != null) {
                        Java2Utils.appendBlockComment(writer, field.getComment());
                    }
                    writer.formatln("public Builder %s(%s key, %s value) {",
                                    camelCase("addTo", field.getName()),
                                    mkType, miType)
                          .begin()
                          .formatln("%s.put(key, value);", fName)
                          .appendln("return this;")
                          .end()
                          .appendln('}')
                          .newline();
                    writer.formatln("public Builder %s() {",
                                    camelCase("clear", field.getName()))
                          .begin()
                          .formatln("%s.clear();", fName)
                          .appendln("return this;")
                          .end()
                          .appendln('}')
                          .newline();
                    break;
                case SET:
                case LIST:
                    TContainer<?, ?> lType = (TContainer<?, ?>) field.descriptor();
                    String liType = mTypeHelper.getSimpleClassName(lType.itemDescriptor());
                    if (field.getComment() != null) {
                        Java2Utils.appendBlockComment(writer, field.getComment());
                    }
                    writer.formatln("public Builder %s(Collection<%s> value) {",
                                    camelCase("set", field.getName()),
                                    liType)
                          .begin()
                          .formatln("%s.clear();", fName)
                          .formatln("%s.addAll(value);", fName)
                          .appendln("return this;")
                          .end()
                          .appendln('}')
                          .newline();
                    if (field.getComment() != null) {
                        Java2Utils.appendBlockComment(writer, field.getComment());
                    }
                    writer.formatln("public Builder %s(%s... values) {",
                                    camelCase("addTo", field.getName()),
                                    liType)
                          .begin()
                          .formatln("for (%s item : values) {", liType)
                          .begin()
                          .formatln("%s.add(item);", fName)
                          .end()
                          .appendln('}')
                          .appendln("return this;")
                          .end()
                          .appendln('}')
                          .newline();
                    writer.formatln("public Builder %s() {",
                                    camelCase("clear", field.getName()))
                          .begin()
                          .formatln("%s.clear();", fName)
                          .appendln("return this;")
                          .end()
                          .appendln('}')
                          .newline();
                    break;
                default:
                    if (field.getComment() != null) {
                        Java2Utils.appendBlockComment(writer, field.getComment());
                    }
                    writer.formatln("public Builder %s(%s value) {",
                                    camelCase("set", field.getName()), vType)
                          .begin()
                          .formatln("%s = value;", fName)
                          .appendln("return this;")
                          .end()
                          .appendln('}')
                          .newline();
                    writer.formatln("public Builder %s() {",
                                    camelCase("clear", field.getName()))
                          .begin()
                          .formatln("%s = null;", fName)
                          .appendln("return this;")
                          .end()
                          .appendln('}')
                          .newline();
                    break;
            }
        }

        writer.appendln("@Override")
              .appendln("public Builder set(int key, Object value) {")
              .begin()
              .appendln("switch (key) {")
              .begin();
        for (TField<?> field : type.getFields()) {
            writer.formatln("case %d: %s((%s) value); break;",
                            field.getKey(),
                            camelCase("set", field.getName()),
                            mTypeHelper.getValueType(field.descriptor()));
        }
        writer.end()
              .appendln('}')
              .appendln("return this;")
              .end()
              .appendln('}')
              .newline();

        appendIsValid(writer, type);

        if (type.getVariant().equals(TMessageVariant.EXCEPTION)) {
            writer.appendln("protected String createMessage() {")
                  .begin()
                  .appendln("StringBuilder builder = new StringBuilder();")
                  .appendln("builder.append('{');")
                  .appendln("boolean first = true;");
            for (TField<?> field : type.getFields()) {
                String fName = camelCase("m", field.getName());
                writer.formatln("if (%s != null) {", fName)
                      .begin()
                      .appendln("if (first) first = false;")
                      .appendln("else builder.append(',');")
                      .formatln("builder.append(\"%s:\");", field.getName());
                switch (field.descriptor().getType()) {
                    case LIST:
                    case SET:
                    case MAP:
                        writer.formatln("builder.append(TTypeUtils.toString(%s));", fName);
                        break;
                    case MESSAGE:
                        writer.formatln("builder.append(TTypeUtils.toString(%s));", fName);
                        break;
                    case STRING:
                        writer.formatln("builder.append(%s);", fName);
                        break;
                    case BINARY:
                        writer.formatln("builder.append(TTypeUtils.toString(%s));",
                                        fName);
                        break;
                    case ENUM:
                        writer.formatln("builder.append(%s.toString());", fName);
                        break;
                    default:
                        writer.formatln("builder.append(%s.toString());", fName);
                        break;
                }
                writer.end()
                      .appendln('}');
            }
            writer.appendln("builder.append('}');")
                  .appendln("return builder.toString();")
                  .end()
                  .appendln('}')
                  .newline();
        }

        writer.appendln("@Override")
              .formatln("public %s build() {", simpleClass)
              .begin()
              .formatln("return new %s(this);", simpleClass)
              .end()
              .appendln('}');

        writer.end()
              .appendln('}');
    }

    private void appendDescriptor(IndentedPrintWriter writer, TStructDescriptor<?> type) throws GeneratorException {
        String simpleClass = mTypeHelper.getSimpleClassName(type);
        String typeClass;
        switch (type.getVariant()) {
            case STRUCT:
                typeClass = TStructDescriptor.class.getSimpleName();
                break;
            case UNION:
                typeClass = TUnionDescriptor.class.getSimpleName();
                break;
            case EXCEPTION:
                typeClass = TExceptionDescriptor.class.getSimpleName();
                break;
            default:
                throw new GeneratorException("Unable to determine type class for " + type.getVariant());
        }

        writer.appendln("@Override")
              .formatln("public %s<%s> descriptor() {", typeClass, simpleClass)
              .begin()
              .appendln("return DESCRIPTOR;")
              .end()
              .appendln('}')
              .newline();

        writer.formatln("public static final %s<%s> DESCRIPTOR = _createDescriptor();", typeClass, simpleClass)
              .newline();

        writer.appendln("private final static class _Factory")
              .begin()
              .formatln("    extends TMessageBuilderFactory<%s> {", simpleClass)
              .appendln("@Override")
              .formatln("public %s.Builder builder() {", simpleClass)
              .begin()
              .formatln("return new %s.Builder();", simpleClass)
              .end()
              .appendln('}')
              .end()
              .appendln('}')
              .newline();

        writer.formatln("private static %s<%s> _createDescriptor() {", typeClass, simpleClass)
              .begin()
              .appendln("List<TField<?>> fieldList = new LinkedList<>();");
        for (TField<?> field : type.getFields()) {
            String provider = mTypeHelper.getProviderName(field.descriptor());
            String defValue = "null";
            if (field.hasDefaultValue()) {
                defValue = String.format("new TDefaultValueProvider<>(%s)", camelCase("kDefault", field.getName()));
            }
            writer.formatln("fieldList.add(new TField<>(null, %d, %s, \"%s\", %s, %s));",
                            field.getKey(),
                            field.getRequired() ? "true" : "false",
                            field.getName(),
                            provider,
                            defValue);
        }
        if (type.getVariant().equals(TMessageVariant.STRUCT)) {
            writer.formatln("return new %s<>(null, \"%s\", \"%s\", fieldList, new _Factory(), %b);",
                            typeClass,
                            type.getPackageName(),
                            type.getName(),
                            type.isCompactible());
        } else {
            writer.formatln("return new %s<>(null, \"%s\", \"%s\", fieldList, new _Factory());",
                            typeClass,
                            type.getPackageName(),
                            type.getName());
        }
        writer.end()
              .appendln('}')
              .newline();

        writer.formatln("public static %sProvider<%s> provider() {", typeClass, simpleClass)
                .begin()
                .formatln("return new %sProvider<%s>() {", typeClass, simpleClass)
                .begin()
                .appendln("@Override")
                .formatln("public %s<%s> descriptor() {", typeClass, simpleClass)
                .begin()
                .appendln("return DESCRIPTOR;")
                .end()
                .appendln('}')
                .end()
                .appendln("};")
                .end()
                .appendln('}')
                .newline();
    }

    private void appendIsValid(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        writer.appendln("@Override")
              .appendln("public boolean isValid() {")
              .begin()
              .appendln("return ")
              .begin("       ");
        if (type.getVariant().equals(TMessageVariant.UNION)) {
            boolean first = true;
            for (TField<?> field : type.getFields()) {
                if (first) first = false;
                else writer.append(" +").appendln("");
                writer.format("(%s != null ? 1 : 0)", camelCase("m", field.getName()));
            }
            writer.append(" == 1");
        } else {
            boolean first = true;
            for (TField<?> field : type.getFields()) {
                if (field.getRequired()) {
                    if (first)
                        first = false;
                    else
                        writer.append(" &&").appendln("");
                    writer.format("%s != null", camelCase("m", field.getName()));
                }
            }
            if (first) {
                writer.append("true");
            }
        }
        writer.end()  // alignment indent
              .append(';')
              .end()
              .appendln('}')
              .newline();
    }

    private void appendObjectToString(IndentedPrintWriter writer) {
        writer.appendln("@Override")
              .appendln("public String toString() {")
              .begin()
              .appendln("return descriptor().getQualifiedName(null) + TTypeUtils.toString(this);")
              .end()
              .appendln("}")
              .newline();
    }

    private void appendObjectEquals(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        String typeName = mTypeHelper.getSimpleClassName(type);
        writer.appendln("@Override")
              .appendln("public boolean equals(Object o) {")
              .begin()
              .formatln("if (o == null || !(o instanceof %s)) return false;", typeName)
              .formatln("%s other = (%s) o;", typeName, typeName)
              .appendln("return ");
        boolean first = true;
        for (TField<?> field : type.getFields()) {
            if (first) first = false;
            else {
                writer.append(" &&")
                      .appendln("       ");
            }
            String fName = camelCase("m", field.getName());
            writer.format("TTypeUtils.equals(%s, other.%s)", fName, fName);
        }
        writer.append(";")
              .end()
              .appendln("}")
              .newline();
    }

    private void appendObjectHashCode(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        writer.appendln("@Override")
              .appendln("public int hashCode() {")
              .begin()
              .formatln("return %s.class.hashCode()", mTypeHelper.getSimpleClassName(type))
              .begin("       ");
        for (TField<?> field : type.getFields()) {
            String fName = camelCase("m", field.getName());
            writer.append(" +")
                  .formatln("TTypeUtils.hashCode(%s)", fName);
        }
        writer.end()
              .append(";")
              .end()
              .appendln("}")
              .newline();
    }

    private void appendInheritedGetter_has(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        writer.appendln("@Override")
              .appendln("public boolean has(int key) {")
              .begin()
              .appendln("switch(key) {")
              .begin();

        for (TField<?> field : type.getFields()) {
            switch (field.descriptor().getType()) {
                case LIST:
                case MAP:
                case SET:
                    writer.formatln("case %d: return %s() > 0;",
                                    field.getKey(),
                                    camelCase("num", field.getName()));
                    break;
                default:
                    writer.formatln("case %d: return %s();",
                                    field.getKey(),
                                    camelCase("has", field.getName()));
                    break;
            }
        }

        writer.appendln("default: return false;")
              .end()
              .appendln('}')
              .end()
              .appendln('}')
              .newline();
    }

    private void appendInheritedGetter_num(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        writer.appendln("@Override")
              .appendln("public int num(int key) {")
              .begin()
              .appendln("switch(key) {")
              .begin();

        for (TField<?> field : type.getFields()) {
            switch (field.descriptor().getType()) {
                case LIST:
                case MAP:
                case SET:
                    writer.formatln("case %d: return %s();",
                                    field.getKey(),
                                    camelCase("num", field.getName()));
                    break;
                default:
                    writer.formatln("case %d: return %s() ? 1 : 0;",
                                    field.getKey(),
                                    camelCase("has", field.getName()));
                    break;
            }
        }

        writer.appendln("default: return 0;")
              .end()
              .appendln('}')
              .end()
              .appendln('}')
              .newline();
    }

    private void appendInheritedGetter_get(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        writer.appendln("@Override")
              .appendln("public Object get(int key) {")
              .begin()
              .appendln("switch(key) {")
              .begin();

        for (TField<?> field : type.getFields()) {
            writer.formatln("case %d: return %s();",
                            field.getKey(), camelCase("get", field.getName()));
        }

        writer.appendln("default: return null;")
              .end()
              .appendln('}')
              .end()
              .appendln('}')
              .newline();
    }

    private void appendFieldDefaultConstants(IndentedPrintWriter writer, TStructDescriptor<?> type) throws GeneratorException {
        boolean hasDefault = false;
        for (TField<?> field : type.getFields()) {
            Object defaultValue = mTypeHelper.getDefaultValue(field);
            if (defaultValue != null) {
                hasDefault = true;
                writer.formatln("private final static %s %s = ",
                                mTypeHelper.getValueType(field.descriptor()),
                                camelCase("kDefault", field.getName()))
                .begin(DBL_INDENT);
                appendTypedValue(writer, defaultValue, field.descriptor());
                writer.append(";")
                      .end();
            }
        }
        if (hasDefault) {
            writer.newline();
        }
    }

    private void appendFieldGetters(IndentedPrintWriter writer, TStructDescriptor<?> type) throws GeneratorException {
        for (TField<?> field : type.getFields()) {
            switch (field.descriptor().getType()) {
                case LIST:
                case SET:
                case MAP:
                    writer.formatln("public int %s() {", camelCase("num", field.getName()))
                          .begin()
                          .formatln("return %s.size();", camelCase("m", field.getName()))
                          .end()
                          .appendln('}')
                          .newline();
                    break;
                default:
                    writer.formatln("public boolean %s() {", camelCase("has", field.getName()))
                          .begin()
                          .formatln("return %s != null;", camelCase("m", field.getName()))
                          .end()
                          .appendln('}')
                          .newline();
                    break;
            }
            if (field.getComment() != null) {
                Java2Utils.appendBlockComment(writer, field.getComment());
            }
            writer.formatln("public %s %s() {",
                            mTypeHelper.getValueType(field.descriptor()),
                            camelCase("get", field.getName()))
                  .begin();

            Object defaultValue = mTypeHelper.getDefaultValue(field);
            if (defaultValue != null) {
                writer.formatln("return %s() ? %s : %s;",
                                camelCase("has", field.getName()),
                                camelCase("m", field.getName()),
                                camelCase("kDefault", field.getName()));
            } else {
                writer.formatln("return %s;",
                                camelCase("m", field.getName()));
            }
            writer.end()
                  .appendln('}')
                  .newline();
        }
    }

    private void appendTypedValue(IndentedPrintWriter writer,
                                  Object defaultValue,
                                  TDescriptor type)
            throws GeneratorException {
        switch (type.getType()) {
            case BINARY:
                throw new GeneratorException("Binary cannot be default value (yet)");
            case BOOL:
                writer.append(defaultValue.toString());
                break;
            case BYTE:
                writer.append("(byte)").append(defaultValue.toString());
                break;
            case I16:
                writer.append("(short)").append(defaultValue.toString());
                break;
            case I32:
                writer.append(defaultValue.toString());
                break;
            case I64:
                writer.append(defaultValue.toString()).append("l");
                break;
            case DOUBLE:
                writer.append(defaultValue.toString()).append("d");
                break;
            case STRING:
                writer.append(JSONObject.quote(defaultValue.toString()));
                break;
            case ENUM:
                writer.format("%s.%s", mTypeHelper.getSimpleClassName(type), defaultValue.toString());
                break;
            case MESSAGE:
                throw new GeneratorException("Message structs cannot have default values");
            case MAP:
            case LIST:
            case SET:
                throw new GeneratorException("Collections cannot have default value.");
        }
    }

    private void appendFieldDeclarations(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        for (TField<?> field : type.getFields()) {
            writer.formatln("private final %s %s;",
                            mTypeHelper.getFieldType(field.descriptor()),
                            camelCase("m", field.getName()));
        }
        writer.newline();
    }

    private void appendConstructor(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        writer.formatln("private %s(Builder builder) {", mTypeHelper.getSimpleClassName(type))
              .begin();
        if (type.getVariant().equals(TMessageVariant.EXCEPTION)) {
            writer.appendln("super(builder.createMessage());")
                  .newline();
        }
        for (TField<?> field : type.getFields()) {
            String fName = camelCase("m", field.getName());
            switch (field.descriptor().getType()) {
                case LIST:
                    writer.formatln("%s = Collections.unmodifiableList(new %s<>(builder.%s));",
                                    fName, mTypeHelper.getSimpleClassName(field.descriptor()), fName);
                    break;
                case SET:
                    writer.formatln("%s = Collections.unmodifiableSet(new %s<>(builder.%s));",
                                    fName, mTypeHelper.getSimpleClassName(field.descriptor()), fName);
                    break;
                case MAP:
                    writer.formatln("%s = Collections.unmodifiableMap(new %s<>(builder.%s));",
                                    fName, mTypeHelper.getSimpleClassName(field.descriptor()), fName);
                    break;
                default:
                    writer.formatln("%s = builder.%s;", fName, fName);
                    break;
            }
        }
        writer.end()
              .appendln('}')
              .newline();
    }

    private void appendClassDefinitionStart(IndentedPrintWriter writer, TStructDescriptor<?> type) {
        writer.formatln("public class %s", mTypeHelper.getSimpleClassName(type))
              .begin(DBL_INDENT);
        if (type.getVariant().equals(TMessageVariant.EXCEPTION)) {
            writer.appendln("extends TException");
        }
        writer.formatln("implements TMessage<%s>, Serializable", mTypeHelper.getSimpleClassName(type));
        if (mAndroid) {
            writer.format(", Parcelable");
        }
        writer.append(" {")
              .end()  // double indent.
              .begin();

        if (type.getVariant().equals(TMessageVariant.EXCEPTION)) {
            writer.formatln("private final static long serialVersionUID = %dL;",
                            Java2Utils.generateSerialVersionUID(type))
                  .newline();
        }
    }

    private void appendClassDefinitionEnd(IndentedPrintWriter writer) {
        writer.end()
              .appendln('}')
              .newline();
    }

    private void addTypeImports(Java2HeaderFormatter header, TDescriptor<?> descriptor) throws GeneratorException {
        switch (descriptor.getType()) {
            case ENUM:
            case MESSAGE:
                // Avoid never-ending recursion (with circular contained
                // structs) by stopping on already included structs and enums.
                String className = mTypeHelper.getQualifiedClassName(descriptor);
                if (!header.hasIncluded(className)) {
                    header.include(className);
                }
                break;
            case LIST:
                TContainer<?, ?> lType = (TContainer<?, ?>) descriptor;
                header.include(java.util.Collection.class.getName());
                header.include(java.util.Collections.class.getName());
                header.include(TList.class.getName());
                header.include(mTypeHelper.getQualifiedClassName(descriptor));
                header.include(mTypeHelper.getQualifiedValueTypeName(descriptor));
                addTypeImports(header, lType.itemDescriptor());
                break;
            case SET:
                TContainer<?, ?> sType = (TContainer<?, ?>) descriptor;
                header.include(java.util.Collection.class.getName());
                header.include(java.util.Collections.class.getName());
                header.include(TSet.class.getName());
                header.include(mTypeHelper.getQualifiedClassName(descriptor));
                header.include(mTypeHelper.getQualifiedValueTypeName(descriptor));
                addTypeImports(header, sType.itemDescriptor());
                break;
            case MAP:
                TMap<?,?> mType = (TMap<?,?>) descriptor;
                header.include(java.util.Collections.class.getName());
                header.include(TMap.class.getName());
                header.include(mTypeHelper.getQualifiedClassName(descriptor));
                header.include(mTypeHelper.getQualifiedValueTypeName(descriptor));
                header.include(mTypeHelper.getQualifiedClassName(mType.itemDescriptor()));
                header.include(mTypeHelper.getQualifiedClassName(mType.keyDescriptor()));
                addTypeImports(header, mType.keyDescriptor());
                addTypeImports(header, mType.itemDescriptor());
                break;
            default:
                header.include(TPrimitive.class.getName());
                break;
        }
    }

    private void appendFileHeader(IndentedPrintWriter writer, TStructDescriptor<?> type) throws GeneratorException, IOException {
        Java2HeaderFormatter header = new Java2HeaderFormatter(mTypeHelper.getJavaPackage(type));
        header.include(java.io.Serializable.class.getName());
        header.include(java.util.LinkedList.class.getName());
        header.include(java.util.List.class.getName());
        header.include(TMessage.class.getName());
        header.include(TMessageBuilder.class.getName());
        header.include(TMessageBuilderFactory.class.getName());
        header.include(TField.class.getName());
        header.include(TTypeUtils.class.getName());
        switch (type.getVariant()) {
            case STRUCT:
                header.include(TStructDescriptor.class.getName());
                header.include(TStructDescriptorProvider.class.getName());
                break;
            case UNION:
                header.include(TUnionDescriptor.class.getName());
                header.include(TUnionDescriptorProvider.class.getName());
                break;
            case EXCEPTION:
                header.include(TException.class.getName());
                header.include(TExceptionDescriptor.class.getName());
                header.include(TExceptionDescriptorProvider.class.getName());
                break;
        }
        for (TField<?> field : type.getFields()) {
            addTypeImports(header, field.descriptor());
            if (field.hasDefaultValue()) {
                header.include(TDefaultValueProvider.class.getName());
            }
        }
        if (mAndroid) {
            header.include("android.os.Parcel");
            header.include("android.os.Parcelable");
        }

        header.format(writer);
    }
}