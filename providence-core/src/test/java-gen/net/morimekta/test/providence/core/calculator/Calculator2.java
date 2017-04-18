package net.morimekta.test.providence.core.calculator;

@SuppressWarnings("unused")
public class Calculator2 {
    public interface Iface extends net.morimekta.test.providence.core.calculator.Calculator.Iface {
        /**
         * @return The extra result.
         * @throws java.io.IOException On providence or non-declared exceptions.
         */
        String extra()
                throws java.io.IOException;
    }

    /**
     * Client implementation for calculator.Calculator2
     */
    public static class Client
            extends net.morimekta.providence.PClient
            implements Iface {
        private final net.morimekta.providence.PServiceCallHandler handler;

        /**
         * Create calculator.Calculator2 service client.
         *
         * @param handler The client handler.
         */
        public Client(net.morimekta.providence.PServiceCallHandler handler) {
            this.handler = handler;
        }

        @Override
        public net.morimekta.test.providence.core.calculator.Operand calculate(
                net.morimekta.test.providence.core.calculator.Operation pOp)
                throws java.io.IOException,
                       net.morimekta.test.providence.core.calculator.CalculateException {
            net.morimekta.test.providence.core.calculator.Calculator.Calculate_request._Builder rq = net.morimekta.test.providence.core.calculator.Calculator.Calculate_request.builder();
            rq.setOp(pOp);

            net.morimekta.providence.PServiceCall call = new net.morimekta.providence.PServiceCall<>("calculate", net.morimekta.providence.PServiceCallType.CALL, getNextSequenceId(), rq.build());
            net.morimekta.providence.PServiceCall resp = handler.handleCall(call, Calculator2.kDescriptor);

            if (resp.getType() == net.morimekta.providence.PServiceCallType.EXCEPTION) {
                throw (net.morimekta.providence.PApplicationException) resp.getMessage();
            }

            net.morimekta.test.providence.core.calculator.Calculator.Calculate_response msg = (net.morimekta.test.providence.core.calculator.Calculator.Calculate_response) resp.getMessage();
            if (msg.unionField() != null) {
                switch (msg.unionField()) {
                    case CE:
                        throw msg.getCe();
                    case SUCCESS:
                        return msg.getSuccess();
                }
            }

            throw new net.morimekta.providence.PApplicationException("Result field for calculator.Calculator2.calculate() not set",
                                                                     net.morimekta.providence.PApplicationExceptionType.MISSING_RESULT);
        }

        @Override
        public void iamalive()
                throws java.io.IOException {
            net.morimekta.test.providence.core.calculator.Calculator.Iamalive_request._Builder rq = net.morimekta.test.providence.core.calculator.Calculator.Iamalive_request.builder();

            net.morimekta.providence.PServiceCall call = new net.morimekta.providence.PServiceCall<>("iamalive", net.morimekta.providence.PServiceCallType.ONEWAY, getNextSequenceId(), rq.build());
            handler.handleCall(call, Calculator2.kDescriptor);
        }

        @Override
        public String extra()
                throws java.io.IOException {
            net.morimekta.test.providence.core.calculator.Calculator2.Extra_request._Builder rq = net.morimekta.test.providence.core.calculator.Calculator2.Extra_request.builder();

            net.morimekta.providence.PServiceCall call = new net.morimekta.providence.PServiceCall<>("extra", net.morimekta.providence.PServiceCallType.CALL, getNextSequenceId(), rq.build());
            net.morimekta.providence.PServiceCall resp = handler.handleCall(call, Calculator2.kDescriptor);

            if (resp.getType() == net.morimekta.providence.PServiceCallType.EXCEPTION) {
                throw (net.morimekta.providence.PApplicationException) resp.getMessage();
            }

            net.morimekta.test.providence.core.calculator.Calculator2.Extra_response msg = (net.morimekta.test.providence.core.calculator.Calculator2.Extra_response) resp.getMessage();
            if (msg.unionField() != null) {
                switch (msg.unionField()) {
                    case SUCCESS:
                        return msg.getSuccess();
                }
            }

            throw new net.morimekta.providence.PApplicationException("Result field for calculator.Calculator2.extra() not set",
                                                                     net.morimekta.providence.PApplicationExceptionType.MISSING_RESULT);
        }
    }

    public static class Processor implements net.morimekta.providence.PProcessor {
        private final Iface impl;
        public Processor(Iface impl) {
            this.impl = impl;
        }

        @Override
        public net.morimekta.providence.descriptor.PService getDescriptor() {
            return kDescriptor;
        }

        @Override
        public <Request extends net.morimekta.providence.PMessage<Request, RequestField>,
                Response extends net.morimekta.providence.PMessage<Response, ResponseField>,
                RequestField extends net.morimekta.providence.descriptor.PField,
                ResponseField extends net.morimekta.providence.descriptor.PField>
        net.morimekta.providence.PServiceCall<Response, ResponseField> handleCall(
                net.morimekta.providence.PServiceCall<Request, RequestField> call,
                net.morimekta.providence.descriptor.PService service)
                throws java.io.IOException,
                       net.morimekta.providence.serializer.SerializerException {
            switch(call.getMethod()) {
                case "calculate": {
                    net.morimekta.test.providence.core.calculator.Calculator.Calculate_response._Builder rsp = net.morimekta.test.providence.core.calculator.Calculator.Calculate_response.builder();
                    try {
                        net.morimekta.test.providence.core.calculator.Calculator.Calculate_request req = (net.morimekta.test.providence.core.calculator.Calculator.Calculate_request) call.getMessage();
                        net.morimekta.test.providence.core.calculator.Operand result =
                                impl.calculate(req.getOp());
                        rsp.setSuccess(result);
                    } catch (net.morimekta.test.providence.core.calculator.CalculateException e) {
                        rsp.setCe(e);
                    }
                    net.morimekta.providence.PServiceCall reply =
                            new net.morimekta.providence.PServiceCall<>(call.getMethod(),
                                                                        net.morimekta.providence.PServiceCallType.REPLY,
                                                                        call.getSequence(),
                                                                        rsp.build());
                    return reply;
                }
                case "iamalive": {
                    net.morimekta.test.providence.core.calculator.Calculator.Iamalive_request req = (net.morimekta.test.providence.core.calculator.Calculator.Iamalive_request) call.getMessage();
                    impl.iamalive();
                    return null;
                }
                case "extra": {
                    net.morimekta.test.providence.core.calculator.Calculator2.Extra_response._Builder rsp = net.morimekta.test.providence.core.calculator.Calculator2.Extra_response.builder();
                    net.morimekta.test.providence.core.calculator.Calculator2.Extra_request req = (net.morimekta.test.providence.core.calculator.Calculator2.Extra_request) call.getMessage();
                    String result =
                            impl.extra();
                    rsp.setSuccess(result);
                    net.morimekta.providence.PServiceCall reply =
                            new net.morimekta.providence.PServiceCall<>(call.getMethod(),
                                                                        net.morimekta.providence.PServiceCallType.REPLY,
                                                                        call.getSequence(),
                                                                        rsp.build());
                    return reply;
                }
                default: {
                    net.morimekta.providence.PApplicationException ex =
                            new net.morimekta.providence.PApplicationException(
                                    "Unknown method \"" + call.getMethod() + "\" on calculator.Calculator2.",
                                    net.morimekta.providence.PApplicationExceptionType.UNKNOWN_METHOD);
                    net.morimekta.providence.PServiceCall reply =
                            new net.morimekta.providence.PServiceCall(call.getMethod(),
                                                                      net.morimekta.providence.PServiceCallType.EXCEPTION,
                                                                      call.getSequence(),
                                                                      ex);
                    return reply;
                }
            }
        }
    }

    public enum Method implements net.morimekta.providence.descriptor.PServiceMethod {
        CALCULATE("calculate", false, net.morimekta.test.providence.core.calculator.Calculator.Calculate_request.kDescriptor, net.morimekta.test.providence.core.calculator.Calculator.Calculate_response.kDescriptor),
        IAMALIVE("iamalive", true, net.morimekta.test.providence.core.calculator.Calculator.Iamalive_request.kDescriptor, null),
        EXTRA("extra", false, net.morimekta.test.providence.core.calculator.Calculator2.Extra_request.kDescriptor, net.morimekta.test.providence.core.calculator.Calculator2.Extra_response.kDescriptor),
        ;

        private final String name;
        private final boolean oneway;
        private final net.morimekta.providence.descriptor.PStructDescriptor request;
        private final net.morimekta.providence.descriptor.PUnionDescriptor response;

        private Method(String name, boolean oneway, net.morimekta.providence.descriptor.PStructDescriptor request, net.morimekta.providence.descriptor.PUnionDescriptor response) {
            this.name = name;
            this.oneway = oneway;
            this.request = request;
            this.response = response;
        }

        public String getName() {
            return name;
        }

        public boolean isOneway() {
            return oneway;
        }

        public net.morimekta.providence.descriptor.PStructDescriptor getRequestType() {
            return request;
        }

        public net.morimekta.providence.descriptor.PUnionDescriptor getResponseType() {
            return response;
        }

        public static Method forName(String name) {
            switch (name) {
                case "calculate": return CALCULATE;
                case "iamalive": return IAMALIVE;
                case "extra": return EXTRA;
            }
            return null;
        }
    }

    private static class _Descriptor extends net.morimekta.providence.descriptor.PService {
        private _Descriptor() {
            super("calculator", "Calculator2", net.morimekta.test.providence.core.calculator.Calculator.provider(), Method.values());
        }

        @Override
        public Method getMethod(String name) {
            return Method.forName(name);
        }
    }

    private static class _Provider implements net.morimekta.providence.descriptor.PServiceProvider {
        @Override
        public net.morimekta.providence.descriptor.PService getService() {
            return kDescriptor;
        }
    }

    public static final net.morimekta.providence.descriptor.PService kDescriptor = new _Descriptor();

    public static net.morimekta.providence.descriptor.PServiceProvider provider() {
        return new _Provider();
    }

    // type --> extra___request
    @SuppressWarnings("unused")
    protected static class Extra_request
            implements net.morimekta.providence.PMessage<Extra_request,Extra_request._Field>,
                       Comparable<Extra_request>,
                       java.io.Serializable,
                       net.morimekta.providence.serializer.rw.BinaryWriter {
        private final static long serialVersionUID = -5728106025551276030L;


        private volatile int tHashCode;

        public Extra_request() {
        }

        private Extra_request(_Builder builder) {
        }

        @Override
        public boolean has(int key) {
            switch(key) {
                default: return false;
            }
        }

        @Override
        public int num(int key) {
            switch(key) {
                default: return 0;
            }
        }

        @Override
        public Object get(int key) {
            switch(key) {
                default: return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || !o.getClass().equals(getClass())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            if (tHashCode == 0) {
                tHashCode = java.util.Objects.hash(
                        Extra_request.class);
            }
            return tHashCode;
        }

        @Override
        public String toString() {
            return "calculator.extra___request" + asString();
        }

        @Override
        public String asString() {
            StringBuilder out = new StringBuilder();
            out.append("{");

            out.append('}');
            return out.toString();
        }

        @Override
        public int compareTo(Extra_request other) {
            int c;

            return 0;
        }

        @Override
        public int writeBinary(net.morimekta.util.io.BigEndianBinaryWriter writer) throws java.io.IOException {
            int length = 0;

            length += writer.writeByte((byte) 0);
            return length;
        }

        @javax.annotation.Nonnull
        @Override
        public _Builder mutate() {
            return new _Builder(this);
        }

        public enum _Field implements net.morimekta.providence.descriptor.PField {
            ;

            private final int mKey;
            private final net.morimekta.providence.descriptor.PRequirement mRequired;
            private final String mName;
            private final net.morimekta.providence.descriptor.PDescriptorProvider mTypeProvider;
            private final net.morimekta.providence.descriptor.PValueProvider<?> mDefaultValue;

            _Field(int key, net.morimekta.providence.descriptor.PRequirement required, String name, net.morimekta.providence.descriptor.PDescriptorProvider typeProvider, net.morimekta.providence.descriptor.PValueProvider<?> defaultValue) {
                mKey = key;
                mRequired = required;
                mName = name;
                mTypeProvider = typeProvider;
                mDefaultValue = defaultValue;
            }

            @Override
            public int getKey() { return mKey; }

            @Override
            public net.morimekta.providence.descriptor.PRequirement getRequirement() { return mRequired; }

            @Override
            public net.morimekta.providence.descriptor.PDescriptor getDescriptor() { return mTypeProvider.descriptor(); }

            @Override
            public String getName() { return mName; }

            @Override
            public boolean hasDefaultValue() { return mDefaultValue != null; }

            @Override
            public Object getDefaultValue() {
                return hasDefaultValue() ? mDefaultValue.get() : null;
            }

            @Override
            public String toString() {
                return net.morimekta.providence.descriptor.PField.toString(this);
            }

            public static _Field forKey(int key) {
                switch (key) {
                }
                return null;
            }

            public static _Field forName(String name) {
                switch (name) {
                }
                return null;
            }
        }

        public static net.morimekta.providence.descriptor.PStructDescriptorProvider<Extra_request,_Field> provider() {
            return new _Provider();
        }

        @Override
        public net.morimekta.providence.descriptor.PStructDescriptor<Extra_request,_Field> descriptor() {
            return kDescriptor;
        }

        public static final net.morimekta.providence.descriptor.PStructDescriptor<Extra_request,_Field> kDescriptor;

        private static class _Descriptor
                extends net.morimekta.providence.descriptor.PStructDescriptor<Extra_request,_Field> {
            public _Descriptor() {
                super("calculator", "extra___request", new _Factory(), true);
            }

            @Override
            public _Field[] getFields() {
                return _Field.values();
            }

            @Override
            public _Field getField(String name) {
                return _Field.forName(name);
            }

            @Override
            public _Field getField(int key) {
                return _Field.forKey(key);
            }
        }

        static {
            kDescriptor = new _Descriptor();
        }

        private final static class _Provider extends net.morimekta.providence.descriptor.PStructDescriptorProvider<Extra_request,_Field> {
            @Override
            public net.morimekta.providence.descriptor.PStructDescriptor<Extra_request,_Field> descriptor() {
                return kDescriptor;
            }
        }

        private final static class _Factory
                extends net.morimekta.providence.PMessageBuilderFactory<Extra_request,_Field> {
            @Override
            public _Builder builder() {
                return new _Builder();
            }
        }

        /**
         * Make a calculator.extra___request builder.
         * @return The builder instance.
         */
        public static _Builder builder() {
            return new _Builder();
        }

        public static class _Builder
                extends net.morimekta.providence.PMessageBuilder<Extra_request,_Field>
                implements net.morimekta.providence.serializer.rw.BinaryReader {
            private java.util.BitSet optionals;
            private java.util.BitSet modified;

            /**
             * Make a calculator.extra___request builder.
             */
            public _Builder() {
                optionals = new java.util.BitSet(0);
                modified = new java.util.BitSet(0);
            }

            /**
             * Make a mutating builder off a base calculator.extra___request.
             *
             * @param base The base extra___request
             */
            public _Builder(Extra_request base) {
                this();

            }

            @javax.annotation.Nonnull
            @Override
            public _Builder merge(Extra_request from) {
                return this;
            }

            @Override
            public boolean equals(Object o) {
                if (o == this) return true;
                if (o == null || !o.getClass().equals(getClass())) return false;
                return true;
            }

            @Override
            public int hashCode() {
                return java.util.Objects.hash(
                        Extra_request.class, optionals);
            }

            @Override
            @SuppressWarnings("unchecked")
            public net.morimekta.providence.PMessageBuilder mutator(int key) {
                switch (key) {
                    default: throw new IllegalArgumentException("Not a message field ID: " + key);
                }
            }

            @javax.annotation.Nonnull
            @Override
            @SuppressWarnings("unchecked")
            public _Builder set(int key, Object value) {
                if (value == null) return clear(key);
                switch (key) {
                    default: break;
                }
                return this;
            }

            @Override
            public boolean isSet(int key) {
                switch (key) {
                    default: break;
                }
                return false;
            }

            @Override
            public boolean isModified(int key) {
                switch (key) {
                    default: break;
                }
                return false;
            }

            @Override
            public _Builder addTo(int key, Object value) {
                switch (key) {
                    default: break;
                }
                return this;
            }

            @javax.annotation.Nonnull
            @Override
            public _Builder clear(int key) {
                switch (key) {
                    default: break;
                }
                return this;
            }

            @Override
            public boolean valid() {
                return true;
            }

            @Override
            public void validate() {
            }

            @javax.annotation.Nonnull
            @Override
            public net.morimekta.providence.descriptor.PStructDescriptor<Extra_request,_Field> descriptor() {
                return kDescriptor;
            }

            @Override
            public void readBinary(net.morimekta.util.io.BigEndianBinaryReader reader, boolean strict) throws java.io.IOException {
                byte type = reader.expectByte();
                while (type != 0) {
                    int field = reader.expectShort();
                    switch (field) {
                        default: {
                            if (strict) {
                                throw new net.morimekta.providence.serializer.SerializerException("No field with id " + field + " exists in calculator.extra___request");
                            } else {
                                net.morimekta.providence.serializer.rw.BinaryFormatUtils.readFieldValue(reader, new net.morimekta.providence.serializer.rw.BinaryFormatUtils.FieldInfo(field, type), null, false);
                            }
                            break;
                        }
                    }
                    type = reader.expectByte();
                }
            }

            @Override
            public Extra_request build() {
                return new Extra_request(this);
            }
        }
    }

    // type <-- extra___response
    @SuppressWarnings("unused")
    protected static class Extra_response
            implements net.morimekta.providence.PUnion<Extra_response,Extra_response._Field>,
                       Comparable<Extra_response>,
                       java.io.Serializable,
                       net.morimekta.providence.serializer.rw.BinaryWriter {
        private final static long serialVersionUID = -6738098496128366484L;

        private final String mSuccess;

        private final _Field tUnionField;

        private volatile int tHashCode;

        /**
         * @param value The union value
         * @return The created union.
         */
        public static Extra_response withSuccess(String value) {
            return new _Builder().setSuccess(value).build();
        }

        private Extra_response(_Builder builder) {
            tUnionField = builder.tUnionField;

            mSuccess = tUnionField == _Field.SUCCESS ? builder.mSuccess : null;
        }

        public boolean hasSuccess() {
            return tUnionField == _Field.SUCCESS && mSuccess != null;
        }

        /**
         * @return The field value
         */
        public String getSuccess() {
            return mSuccess;
        }

        @Override
        public boolean has(int key) {
            switch(key) {
                case 0: return hasSuccess();
                default: return false;
            }
        }

        @Override
        public int num(int key) {
            switch(key) {
                case 0: return hasSuccess() ? 1 : 0;
                default: return 0;
            }
        }

        @Override
        public Object get(int key) {
            switch(key) {
                case 0: return getSuccess();
                default: return null;
            }
        }

        @Override
        public _Field unionField() {
            return tUnionField;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || !o.getClass().equals(getClass())) return false;
            Extra_response other = (Extra_response) o;
            return java.util.Objects.equals(tUnionField, other.tUnionField) &&
                   java.util.Objects.equals(mSuccess, other.mSuccess);
        }

        @Override
        public int hashCode() {
            if (tHashCode == 0) {
                tHashCode = java.util.Objects.hash(
                        Extra_response.class,
                        _Field.SUCCESS, mSuccess);
            }
            return tHashCode;
        }

        @Override
        public String toString() {
            return "calculator.extra___response" + asString();
        }

        @Override
        public String asString() {
            StringBuilder out = new StringBuilder();
            out.append("{");

            switch (tUnionField) {
                case SUCCESS: {
                    out.append("success:")
                       .append('\"').append(net.morimekta.util.Strings.escape(mSuccess)).append('\"');
                    break;
                }
            }
            out.append('}');
            return out.toString();
        }

        @Override
        public int compareTo(Extra_response other) {
            int c = tUnionField.compareTo(other.tUnionField);
            if (c != 0) return c;

            switch (tUnionField) {
                case SUCCESS:
                    return mSuccess.compareTo(other.mSuccess);
                default: return 0;
            }
        }

        @Override
        public int writeBinary(net.morimekta.util.io.BigEndianBinaryWriter writer) throws java.io.IOException {
            int length = 0;

            if (tUnionField != null) {
                switch (tUnionField) {
                    case SUCCESS: {
                        length += writer.writeByte((byte) 11);
                        length += writer.writeShort((short) 0);
                        net.morimekta.util.Binary tmp_1 = net.morimekta.util.Binary.wrap(mSuccess.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        length += writer.writeUInt32(tmp_1.length());
                        length += writer.writeBinary(tmp_1);
                        break;
                    }
                    default: break;
                }
            }
            length += writer.writeByte((byte) 0);
            return length;
        }

        @javax.annotation.Nonnull
        @Override
        public _Builder mutate() {
            return new _Builder(this);
        }

        public enum _Field implements net.morimekta.providence.descriptor.PField {
            SUCCESS(0, net.morimekta.providence.descriptor.PRequirement.OPTIONAL, "success", net.morimekta.providence.descriptor.PPrimitive.STRING.provider(), null),
            ;

            private final int mKey;
            private final net.morimekta.providence.descriptor.PRequirement mRequired;
            private final String mName;
            private final net.morimekta.providence.descriptor.PDescriptorProvider mTypeProvider;
            private final net.morimekta.providence.descriptor.PValueProvider<?> mDefaultValue;

            _Field(int key, net.morimekta.providence.descriptor.PRequirement required, String name, net.morimekta.providence.descriptor.PDescriptorProvider typeProvider, net.morimekta.providence.descriptor.PValueProvider<?> defaultValue) {
                mKey = key;
                mRequired = required;
                mName = name;
                mTypeProvider = typeProvider;
                mDefaultValue = defaultValue;
            }

            @Override
            public int getKey() { return mKey; }

            @Override
            public net.morimekta.providence.descriptor.PRequirement getRequirement() { return mRequired; }

            @Override
            public net.morimekta.providence.descriptor.PDescriptor getDescriptor() { return mTypeProvider.descriptor(); }

            @Override
            public String getName() { return mName; }

            @Override
            public boolean hasDefaultValue() { return mDefaultValue != null; }

            @Override
            public Object getDefaultValue() {
                return hasDefaultValue() ? mDefaultValue.get() : null;
            }

            @Override
            public String toString() {
                return net.morimekta.providence.descriptor.PField.toString(this);
            }

            public static _Field forKey(int key) {
                switch (key) {
                    case 0: return _Field.SUCCESS;
                }
                return null;
            }

            public static _Field forName(String name) {
                switch (name) {
                    case "success": return _Field.SUCCESS;
                }
                return null;
            }
        }

        public static net.morimekta.providence.descriptor.PUnionDescriptorProvider<Extra_response,_Field> provider() {
            return new _Provider();
        }

        @Override
        public net.morimekta.providence.descriptor.PUnionDescriptor<Extra_response,_Field> descriptor() {
            return kDescriptor;
        }

        public static final net.morimekta.providence.descriptor.PUnionDescriptor<Extra_response,_Field> kDescriptor;

        private static class _Descriptor
                extends net.morimekta.providence.descriptor.PUnionDescriptor<Extra_response,_Field> {
            public _Descriptor() {
                super("calculator", "extra___response", new _Factory(), true);
            }

            @Override
            public _Field[] getFields() {
                return _Field.values();
            }

            @Override
            public _Field getField(String name) {
                return _Field.forName(name);
            }

            @Override
            public _Field getField(int key) {
                return _Field.forKey(key);
            }
        }

        static {
            kDescriptor = new _Descriptor();
        }

        private final static class _Provider extends net.morimekta.providence.descriptor.PUnionDescriptorProvider<Extra_response,_Field> {
            @Override
            public net.morimekta.providence.descriptor.PUnionDescriptor<Extra_response,_Field> descriptor() {
                return kDescriptor;
            }
        }

        private final static class _Factory
                extends net.morimekta.providence.PMessageBuilderFactory<Extra_response,_Field> {
            @Override
            public _Builder builder() {
                return new _Builder();
            }
        }

        /**
         * Make a calculator.extra___response builder.
         * @return The builder instance.
         */
        public static _Builder builder() {
            return new _Builder();
        }

        public static class _Builder
                extends net.morimekta.providence.PMessageBuilder<Extra_response,_Field>
                implements net.morimekta.providence.serializer.rw.BinaryReader {
            private _Field tUnionField;

            private boolean modified;

            private String mSuccess;

            /**
             * Make a calculator.extra___response builder.
             */
            public _Builder() {
                modified = false;
            }

            /**
             * Make a mutating builder off a base calculator.extra___response.
             *
             * @param base The base extra___response
             */
            public _Builder(Extra_response base) {
                this();

                tUnionField = base.tUnionField;

                mSuccess = base.mSuccess;
            }

            @javax.annotation.Nonnull
            @Override
            public _Builder merge(Extra_response from) {
                if (from.unionField() == null) {
                    return this;
                }

                switch (from.unionField()) {
                    case SUCCESS: {
                        setSuccess(from.getSuccess());
                        break;
                    }
                }
                return this;
            }

            /**
             * Sets the value of success.
             *
             * @param value The new value
             * @return The builder
             */
            @javax.annotation.Nonnull
            public _Builder setSuccess(String value) {
                tUnionField = _Field.SUCCESS;
                modified = true;
                mSuccess = value;
                return this;
            }

            /**
             * Checks for presence of the success field.
             *
             * @return True if success has been set.
             */
            public boolean isSetSuccess() {
                return tUnionField == _Field.SUCCESS;
            }

            /**
             * Clears the success field.
             *
             * @return The builder
             */
            @javax.annotation.Nonnull
            public _Builder clearSuccess() {
                if (tUnionField == _Field.SUCCESS) tUnionField = null;
                modified = true;
                mSuccess = null;
                return this;
            }

            /**
             * Gets the value of the contained success.
             *
             * @return The field value
             */
            public String getSuccess() {
                return mSuccess;
            }

            /**
             * Checks if extra___response has been modified since the _Builder was created.
             *
             * @return True if extra___response has been modified.
             */
            public boolean isUnionModified() {
                return modified;
            }

            @Override
            public boolean equals(Object o) {
                if (o == this) return true;
                if (o == null || !o.getClass().equals(getClass())) return false;
                Extra_response._Builder other = (Extra_response._Builder) o;
                return java.util.Objects.equals(tUnionField, other.tUnionField) &&
                       java.util.Objects.equals(mSuccess, other.mSuccess);
            }

            @Override
            public int hashCode() {
                return java.util.Objects.hash(
                        Extra_response.class,
                        _Field.SUCCESS, mSuccess);
            }

            @Override
            @SuppressWarnings("unchecked")
            public net.morimekta.providence.PMessageBuilder mutator(int key) {
                switch (key) {
                    default: throw new IllegalArgumentException("Not a message field ID: " + key);
                }
            }

            @javax.annotation.Nonnull
            @Override
            @SuppressWarnings("unchecked")
            public _Builder set(int key, Object value) {
                if (value == null) return clear(key);
                switch (key) {
                    case 0: setSuccess((String) value); break;
                    default: break;
                }
                return this;
            }

            @Override
            public boolean isSet(int key) {
                switch (key) {
                    case 0: return tUnionField == _Field.SUCCESS;
                    default: break;
                }
                return false;
            }

            @Override
            public boolean isModified(int key) {
                return modified;
            }

            @Override
            public _Builder addTo(int key, Object value) {
                switch (key) {
                    default: break;
                }
                return this;
            }

            @javax.annotation.Nonnull
            @Override
            public _Builder clear(int key) {
                switch (key) {
                    case 0: clearSuccess(); break;
                    default: break;
                }
                return this;
            }

            @Override
            public boolean valid() {
                if (tUnionField == null) {
                    return false;
                }

                switch (tUnionField) {
                    case SUCCESS: return mSuccess != null;
                    default: return true;
                }
            }

            @Override
            public void validate() {
                if (!valid()) {
                    throw new java.lang.IllegalStateException("No union field set in calculator.extra___response");
                }
            }

            @javax.annotation.Nonnull
            @Override
            public net.morimekta.providence.descriptor.PUnionDescriptor<Extra_response,_Field> descriptor() {
                return kDescriptor;
            }

            @Override
            public void readBinary(net.morimekta.util.io.BigEndianBinaryReader reader, boolean strict) throws java.io.IOException {
                byte type = reader.expectByte();
                while (type != 0) {
                    int field = reader.expectShort();
                    switch (field) {
                        case 0: {
                            if (type == 11) {
                                int len_1 = reader.expectUInt32();
                                mSuccess = new String(reader.expectBytes(len_1), java.nio.charset.StandardCharsets.UTF_8);
                                tUnionField = _Field.SUCCESS;
                            } else {
                                throw new net.morimekta.providence.serializer.SerializerException("Wrong type " + net.morimekta.providence.PType.nameForId(type) + "(" + type + ") for calculator.extra___response.success, should be message(12)");
                            }
                            break;
                        }
                        default: {
                            if (strict) {
                                throw new net.morimekta.providence.serializer.SerializerException("No field with id " + field + " exists in calculator.extra___response");
                            } else {
                                net.morimekta.providence.serializer.rw.BinaryFormatUtils.readFieldValue(reader, new net.morimekta.providence.serializer.rw.BinaryFormatUtils.FieldInfo(field, type), null, false);
                            }
                            break;
                        }
                    }
                    type = reader.expectByte();
                }
            }

            @Override
            public Extra_response build() {
                return new Extra_response(this);
            }
        }
    }

    private Calculator2() {}
}