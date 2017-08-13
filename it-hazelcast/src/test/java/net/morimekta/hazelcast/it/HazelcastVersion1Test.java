package net.morimekta.hazelcast.it;

import net.morimekta.test.hazelcast.v1.OptionalFields;
import net.morimekta.test.hazelcast.v1.OptionalListFields;
import net.morimekta.test.hazelcast.v1.OptionalMapFields;
import net.morimekta.test.hazelcast.v1.OptionalMapListFields;
import net.morimekta.test.hazelcast.v1.OptionalMapSetFields;
import net.morimekta.test.hazelcast.v1.OptionalSetFields;
import net.morimekta.test.hazelcast.v1.UnionFields;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TBD
 */
public class HazelcastVersion1Test extends GenericMethods {
    static HazelcastInstance instance1;
    static HazelcastInstance instance2;

    @BeforeClass
    public static void setUpHazelcast() {
        instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        instance2 = Hazelcast.newHazelcastInstance(getV1Config());
    }

    @Test
    public void testMapIntegrityAll() throws InterruptedException {
        generator.getBaseContext().setDefaultFillRate(1.0);
        assertMapIntegrity(instance1, instance2, OptionalFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalListFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalSetFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalMapFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalMapListFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalMapSetFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, UnionFields.kDescriptor);
    }

    @Test
    public void testMapIntegrityRand() throws InterruptedException {
        generator.getBaseContext().setDefaultFillRate(0.5);
        assertMapIntegrity(instance1, instance2, OptionalFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalListFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalSetFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalMapFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalMapListFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, OptionalMapSetFields.kDescriptor);
        assertMapIntegrity(instance1, instance2, UnionFields.kDescriptor);
    }
}
