package net.morimekta.providence.model;

import java.io.Serializable;

import net.morimekta.providence.PMessage;
import net.morimekta.providence.PMessageBuilder;
import net.morimekta.providence.PMessageBuilderFactory;
import net.morimekta.providence.PType;
import net.morimekta.providence.descriptor.PDescriptor;
import net.morimekta.providence.descriptor.PDescriptorProvider;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.providence.descriptor.PPrimitive;
import net.morimekta.providence.descriptor.PRequirement;
import net.morimekta.providence.descriptor.PStructDescriptor;
import net.morimekta.providence.descriptor.PStructDescriptorProvider;
import net.morimekta.providence.descriptor.PValueProvider;
import net.morimekta.providence.util.PTypeUtils;

/** <name> (= <value>) */
@SuppressWarnings("unused")
public class EnumValue
        implements PMessage<EnumValue>, Serializable {
    private final static int kDefaultValue = 0;

    private final String mComment;
    private final String mName;
    private final int mValue;

    private EnumValue(_Builder builder) {
        mComment = builder.mComment;
        mName = builder.mName;
        if (builder.mValue != null) {
            mValue = builder.mValue;
        } else {
            mValue = kDefaultValue;
        }
    }

    public EnumValue(String pComment,
                     String pName,
                     int pValue) {
        mComment = pComment;
        mName = pName;
        mValue = pValue;
    }

    public boolean hasComment() {
        return mComment != null;
    }

    public String getComment() {
        return mComment;
    }

    public boolean hasName() {
        return mName != null;
    }

    public String getName() {
        return mName;
    }

    public boolean hasValue() {
        return true;
    }

    public int getValue() {
        return mValue;
    }

    @Override
    public boolean has(int key) {
        switch(key) {
            case 1: return hasComment();
            case 2: return hasName();
            case 3: return true;
            default: return false;
        }
    }

    @Override
    public int num(int key) {
        switch(key) {
            case 1: return hasComment() ? 1 : 0;
            case 2: return hasName() ? 1 : 0;
            case 3: return 1;
            default: return 0;
        }
    }

    @Override
    public Object get(int key) {
        switch(key) {
            case 1: return getComment();
            case 2: return getName();
            case 3: return getValue();
            default: return null;
        }
    }

    @Override
    public boolean isCompact() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return descriptor().isSimple();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof EnumValue)) return false;
        EnumValue other = (EnumValue) o;
        return PTypeUtils.equals(mComment, other.mComment) &&
               PTypeUtils.equals(mName, other.mName) &&
               PTypeUtils.equals(mValue, other.mValue);
    }

    @Override
    public int hashCode() {
        return EnumValue.class.hashCode() +
               PTypeUtils.hashCode(_Field.COMMENT,mComment) +
               PTypeUtils.hashCode(_Field.NAME,mName) +
               PTypeUtils.hashCode(_Field.VALUE,mValue);
    }

    @Override
    public String toString() {
        return descriptor().getQualifiedName(null) + PTypeUtils.toString(this);
    }

    public enum _Field implements PField {
        COMMENT(1, PRequirement.DEFAULT, "comment", PPrimitive.STRING.provider(), null),
        NAME(2, PRequirement.REQUIRED, "name", PPrimitive.STRING.provider(), null),
        VALUE(3, PRequirement.DEFAULT, "value", PPrimitive.I32.provider(), null),
        ;

        private final int mKey;
        private final PRequirement mRequired;
        private final String mName;
        private final PDescriptorProvider<?> mTypeProvider;
        private final PValueProvider<?> mDefaultValue;

        _Field(int key, PRequirement required, String name, PDescriptorProvider<?> typeProvider, PValueProvider<?> defaultValue) {
            mKey = key;
            mRequired = required;
            mName = name;
            mTypeProvider = typeProvider;
            mDefaultValue = defaultValue;
        }

        @Override
        public String getComment() { return null; }

        @Override
        public int getKey() { return mKey; }

        @Override
        public PRequirement getRequirement() { return mRequired; }

        @Override
        public PType getType() { return getDescriptor().getType(); }

        @Override
        public PDescriptor<?> getDescriptor() { return mTypeProvider.descriptor(); }

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
            StringBuilder builder = new StringBuilder();
            builder.append(EnumValue.class.getSimpleName())
                   .append('{')
                   .append(mKey)
                   .append(": ");
            if (mRequired != PRequirement.DEFAULT) {
                builder.append(mRequired.label).append(" ");
            }
            builder.append(getDescriptor().getQualifiedName(null))
                   .append(' ')
                   .append(mName)
                   .append('}');
            return builder.toString();
        }

        public static _Field forKey(int key) {
            switch (key) {
                case 1: return _Field.COMMENT;
                case 2: return _Field.NAME;
                case 3: return _Field.VALUE;
                default: return null;
            }
        }

        public static _Field forName(String name) {
            switch (name) {
                case "comment": return _Field.COMMENT;
                case "name": return _Field.NAME;
                case "value": return _Field.VALUE;
            }
            return null;
        }
    }

    public static PStructDescriptorProvider<EnumValue,_Field> provider() {
        return new _Provider();
    }

    @Override
    public PStructDescriptor<EnumValue,_Field> descriptor() {
        return kDescriptor;
    }

    public static final PStructDescriptor<EnumValue,_Field> kDescriptor;

    private static class _Descriptor
            extends PStructDescriptor<EnumValue,_Field> {
        public _Descriptor() {
            super(null, "model", "EnumValue", new _Factory(), true, false);
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

    private final static class _Provider extends PStructDescriptorProvider<EnumValue,_Field> {
        @Override
        public PStructDescriptor<EnumValue,_Field> descriptor() {
            return kDescriptor;
        }
    }

    private final static class _Factory
            extends PMessageBuilderFactory<EnumValue> {
        @Override
        public _Builder builder() {
            return new _Builder();
        }
    }

    @Override
    public _Builder mutate() {
        return new _Builder(this);
    }

    public static _Builder builder() {
        return new _Builder();
    }

    public static class _Builder
            extends PMessageBuilder<EnumValue> {
        private String mComment;
        private String mName;
        private Integer mValue;

        public _Builder() {
        }

        public _Builder(EnumValue base) {
            this();

            mComment = base.mComment;
            mName = base.mName;
            mValue = base.mValue;
        }

        public _Builder setComment(String value) {
            mComment = value;
            return this;
        }

        public _Builder clearComment() {
            mComment = null;
            return this;
        }

        public _Builder setName(String value) {
            mName = value;
            return this;
        }

        public _Builder clearName() {
            mName = null;
            return this;
        }

        public _Builder setValue(int value) {
            mValue = value;
            return this;
        }

        public _Builder clearValue() {
            mValue = null;
            return this;
        }

        @Override
        public _Builder set(int key, Object value) {
            switch (key) {
                case 1: setComment((String) value); break;
                case 2: setName((String) value); break;
                case 3: setValue((int) value); break;
            }
            return this;
        }

        @Override
        public boolean isValid() {
            return mName != null;
        }

        @Override
        public EnumValue build() {
            return new EnumValue(this);
        }
    }
}
