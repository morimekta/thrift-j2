package net.morimekta.hazelcast.it;

import net.morimekta.test.hazelcast.v1.AllFields;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.Test;

import java.util.stream.Collectors;

import static net.morimekta.providence.testing.ProvidenceMatchers.equalToMessage;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * TBD
 */
public class HazelcastVersion1Test extends GenericMethods {

    @Test
    public void testVersion1OptionalFieldsAll() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalFields expected = generator.nextOptionalFieldsV1(true);

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalFields actual = readMap.get(key)
                                                                       .build();

        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalFields newExpected = actual.mutate()
                                                                           .setStringValue(
                                                                                   actual.getStringValue() + "asdf")
                                                                           .setDoubleValue(
                                                                                   actual.getDoubleValue() - 0.12345)
                                                                           .setBooleanValue(!actual.isBooleanValue())
                                                                           .setIntegerValue(
                                                                                   actual.getIntegerValue() - 12345)
                                                                           .setLongValue(actual.getLongValue() - 123456)
                                                                           .setShortValue((short) (
                                                                                   actual.getShortValue() - 1234))
                                                                           .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalFields newActual = writeMap.get(key)
                                                                           .build();

        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalFieldsRand() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalFields expected = generator.nextOptionalFieldsV1();

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalFields actual = readMap.get(key)
                                                                       .build();

        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalFields newExpected = actual.mutate()
                                                                           .setStringValue(
                                                                                   actual.getStringValue() + "asdf")
                                                                           .setDoubleValue(
                                                                                   actual.getDoubleValue() - 0.12345)
                                                                           .setBooleanValue(!actual.isBooleanValue())
                                                                           .setIntegerValue(
                                                                                   actual.getIntegerValue() - 12345)
                                                                           .setLongValue(actual.getLongValue() - 123456)
                                                                           .setShortValue((short) (
                                                                                   actual.getShortValue() - 1234))
                                                                           .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalFields newActual = writeMap.get(key)
                                                                           .build();

        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalListFieldsAll() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalListFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalListFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalListFields expected = generator.nextOptionalListFieldsV1(true);

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalListFields actual = readMap.get(key)
                                                                           .build();

        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalListFields newExpected = actual.mutate()
                                                                               .setStringValue(actual.getStringValue()
                                                                                                     .stream()
                                                                                                     .filter(t -> t.contains(
                                                                                                             "ab"))
                                                                                                     .collect(Collectors.toList()))
                                                                               .setDoubleValue(actual.getDoubleValue()
                                                                                                     .stream()
                                                                                                     .filter(t -> t <
                                                                                                                  0.5)
                                                                                                     .collect(Collectors.toList()))
                                                                               .setBooleanValues(actual.getBooleanValues()
                                                                                                       .stream()
                                                                                                       .map(t -> !t)
                                                                                                       .limit(25)
                                                                                                       .collect(
                                                                                                               Collectors.toList()))
                                                                               .setIntegerValue(actual.getIntegerValue()
                                                                                                      .stream()
                                                                                                      .limit(25)
                                                                                                      .collect(
                                                                                                              Collectors.toList()))
                                                                               .setLongValue(actual.getLongValue()
                                                                                                   .stream()
                                                                                                   .limit(23)
                                                                                                   .collect(Collectors.toList()))
                                                                               .setShortValues(actual.getShortValues()
                                                                                                     .stream()
                                                                                                     .limit(12)
                                                                                                     .collect(Collectors.toList()))
                                                                               .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalListFields newActual = writeMap.get(key)
                                                                               .build();

        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalListFieldsRand() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalListFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalListFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalListFields expected = generator.nextOptionalListFieldsV1();

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalListFields actual = readMap.get(key)
                                                                           .build();

        assertThat(expected.toString(), is(actual.toString()));
        assertThat(expected, is(equalToMessage(actual)));
        assertThat(expected.hashCode(), is(actual.hashCode()));
        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalListFields._Builder newBuilder = actual.mutate();
        if (actual.hasStringValue()) {
            newBuilder.setStringValue(actual.getStringValue()
                                            .stream()
                                            .filter(t -> t.contains("ab"))
                                            .collect(Collectors.toList()));
        }
        if (actual.hasDoubleValue()) {
            newBuilder.setDoubleValue(actual.getDoubleValue()
                                            .stream()
                                            .filter(t -> t < 0.5)
                                            .collect(Collectors.toList()));
        }
        if (actual.hasBooleanValues()) {
            newBuilder.setBooleanValues(actual.getBooleanValues()
                                              .stream()
                                              .map(t -> !t)
                                              .limit(actual.numBooleanValues() -
                                                     rand.nextInt(actual.numBooleanValues()))
                                              .collect(Collectors.toList()));
        }
        if (actual.hasIntegerValue()) {
            newBuilder.setIntegerValue(actual.getIntegerValue()
                                             .stream()
                                             .limit(actual.numIntegerValue() - rand.nextInt(actual.numIntegerValue()))
                                             .collect(Collectors.toList()));
        }
        if (actual.hasLongValue()) {
            newBuilder.setLongValue(actual.getLongValue()
                                          .stream()
                                          .limit(actual.numLongValue() - rand.nextInt(actual.numLongValue()))
                                          .collect(Collectors.toList()));
        }
        if (actual.hasShortValues()) {
            newBuilder.setShortValues(actual.getShortValues()
                                            .stream()
                                            .limit(actual.numShortValues() - rand.nextInt(actual.numShortValues()))
                                            .collect(Collectors.toList()));
        }

        net.morimekta.test.hazelcast.v1.OptionalListFields newExpected = newBuilder.build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalListFields newActual = writeMap.get(key)
                                                                               .build();

        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalSetFieldsAll() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalSetFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalSetFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalSetFields expected = generator.nextOptionalSetFieldsV1(true);

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalSetFields actual = readMap.get(key)
                                                                          .build();

        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalSetFields newExpected = actual.mutate()
                                                                              .setStringValue(actual.getStringValue()
                                                                                                    .stream()
                                                                                                    .filter(t -> t.contains(
                                                                                                            "ab"))
                                                                                                    .collect(Collectors.toList()))
                                                                              .setDoubleValue(actual.getDoubleValue()
                                                                                                    .stream()
                                                                                                    .filter(t -> t <
                                                                                                                 0.5)
                                                                                                    .collect(Collectors.toList()))
                                                                              .setBooleanValues(actual.getBooleanValues()
                                                                                                      .stream()
                                                                                                      .map(t -> !t)
                                                                                                      .limit(25)
                                                                                                      .collect(
                                                                                                              Collectors.toList()))
                                                                              .setIntegerValue(actual.getIntegerValue()
                                                                                                     .stream()
                                                                                                     .limit(25)
                                                                                                     .collect(Collectors.toList()))
                                                                              .setLongValue(actual.getLongValue()
                                                                                                  .stream()
                                                                                                  .limit(23)
                                                                                                  .collect(Collectors.toList()))
                                                                              .setShortValues(actual.getShortValues()
                                                                                                    .stream()
                                                                                                    .limit(12)
                                                                                                    .collect(Collectors.toList()))
                                                                              .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalSetFields newActual = writeMap.get(key)
                                                                              .build();

        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalSetFieldsRand() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalSetFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalSetFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalSetFields expected = generator.nextOptionalSetFieldsV1();

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalSetFields actual = readMap.get(key)
                                                                          .build();

        assertThat(expected, is(equalToMessage(actual)));
        assertThat(expected.hashCode(), is(actual.hashCode()));
        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalSetFields._Builder newBuilder = actual.mutate();
        if (actual.hasStringValue()) {
            newBuilder.setStringValue(actual.getStringValue()
                                            .stream()
                                            .filter(t -> t.contains("ab"))
                                            .collect(Collectors.toList()));
        }
        if (actual.hasDoubleValue()) {
            newBuilder.setDoubleValue(actual.getDoubleValue()
                                            .stream()
                                            .filter(t -> t < 0.5)
                                            .collect(Collectors.toList()));
        }
        if (actual.hasBooleanValues()) {
            newBuilder.setBooleanValues(actual.getBooleanValues()
                                              .stream()
                                              .map(t -> !t)
                                              .limit(actual.numBooleanValues() -
                                                     rand.nextInt(actual.numBooleanValues()))
                                              .collect(Collectors.toList()));
        }
        if (actual.hasIntegerValue()) {
            newBuilder.setIntegerValue(actual.getIntegerValue()
                                             .stream()
                                             .limit(actual.numIntegerValue() - rand.nextInt(actual.numIntegerValue()))
                                             .collect(Collectors.toList()));
        }
        if (actual.hasLongValue()) {
            newBuilder.setLongValue(actual.getLongValue()
                                          .stream()
                                          .limit(actual.numLongValue() - rand.nextInt(actual.numLongValue()))
                                          .collect(Collectors.toList()));
        }
        if (actual.hasShortValues()) {
            newBuilder.setShortValues(actual.getShortValues()
                                            .stream()
                                            .limit(actual.numShortValues() - rand.nextInt(actual.numShortValues()))
                                            .collect(Collectors.toList()));
        }

        net.morimekta.test.hazelcast.v1.OptionalSetFields newExpected = newBuilder.build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalSetFields newActual = writeMap.get(key)
                                                                              .build();

        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalMapFieldsAll() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalMapFields expected = generator.nextOptionalMapFieldsV1(true);

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapFields actual = readMap.get(key)
                                                                          .build();

        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalMapFields newExpected = actual.mutate()
                                                                              .setIntegerValue(generator.entities.nextIntegerMap())
                                                                              .setDoubleValue(generator.entities.nextDoubleMap())
                                                                              .setLongValue(generator.entities.nextLongMap())
                                                                              .setShortValue(generator.entities.nextShortMap())
                                                                              .setStringValue(generator.entities.nextStringMap())
                                                                              .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapFields newActual = writeMap.get(key)
                                                                              .build();

        for (net.morimekta.test.hazelcast.v1.OptionalMapFields._Field field : net.morimekta.test.hazelcast.v1.OptionalMapFields._Field.values()) {
            assertThat(actual.has(field), is(true));
            assertThat(expected.has(field), is(true));
            assertThat(newActual.has(field), is(true));
            assertThat(newExpected.has(field), is(true));
        }
        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalMapFieldsRand() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalMapFields expected = generator.nextOptionalMapFieldsV1();

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapFields actual = readMap.get(key)
                                                                          .build();

        assertThat(expected, is(equalToMessage(actual)));

        net.morimekta.test.hazelcast.v1.OptionalMapFields newExpected = actual.mutate()
                                                                              .setIntegerValue(generator.entities.nextIntegerMap())
                                                                              .setDoubleValue(generator.entities.nextDoubleMap())
                                                                              .setLongValue(generator.entities.nextLongMap())
                                                                              .setShortValue(generator.entities.nextShortMap())
                                                                              .setStringValue(generator.entities.nextStringMap())
                                                                              .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapFields newActual = writeMap.get(key)
                                                                              .build();

        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalMapListFieldsAll() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapListFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapListFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalMapListFields expected = generator.nextOptionalMapListFieldsV1(true);

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapListFields actual = readMap.get(key)
                                                                              .build();

        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalMapListFields newExpected = actual.mutate()
                                                                                  .setIntegerValueList(generator.entities.nextIntegerListMap())
                                                                                  .setDoubleValueList(generator.entities.nextDoubleListMap())
                                                                                  .setLongValueList(generator.entities.nextLongListMap())
                                                                                  .setShortValueList(generator.entities.nextShortListMap())
                                                                                  .setStringValueList(generator.entities.nextStringListMap())
                                                                                  .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapListFields newActual = writeMap.get(key)
                                                                                  .build();

        for (net.morimekta.test.hazelcast.v1.OptionalMapListFields._Field field : net.morimekta.test.hazelcast.v1.OptionalMapListFields._Field.values()) {
            assertThat(actual.has(field), is(true));
            assertThat(expected.has(field), is(true));
            assertThat(newActual.has(field), is(true));
            assertThat(newExpected.has(field), is(true));
        }
        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalMapListFieldsRand() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapListFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapListFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalMapListFields expected = generator.nextOptionalMapListFieldsV1();

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapListFields actual = readMap.get(key)
                                                                              .build();

        assertThat(expected, is(equalToMessage(actual)));

        net.morimekta.test.hazelcast.v1.OptionalMapListFields newExpected = actual.mutate()
                                                                                  .setIntegerValueList(generator.entities.nextIntegerListMap())
                                                                                  .setDoubleValueList(generator.entities.nextDoubleListMap())
                                                                                  .setLongValueList(generator.entities.nextLongListMap())
                                                                                  .setShortValueList(generator.entities.nextShortListMap())
                                                                                  .setStringValueList(generator.entities.nextStringListMap())
                                                                                  .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapListFields newActual = writeMap.get(key)
                                                                                  .build();

        assertThat(newExpected.toString(), is(newActual.toString()));
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalMapSetFieldsAll() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapSetFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapSetFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalMapSetFields expected = generator.nextOptionalMapSetFieldsV1(true);

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapSetFields actual = readMap.get(key)
                                                                             .build();

        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.OptionalMapSetFields newExpected = actual.mutate()
                                                                                 .setIntegerValueSet(generator.entities.nextIntegerSetMap())
                                                                                 .setDoubleValueSet(generator.entities.nextDoubleSetMap())
                                                                                 .setLongValueSet(generator.entities.nextLongSetMap())
                                                                                 .setShortValueSet(generator.entities.nextShortSetMap())
                                                                                 .setStringValueSet(generator.entities.nextStringSetMap())
                                                                                 .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapSetFields newActual = writeMap.get(key)
                                                                                 .build();

        for (net.morimekta.test.hazelcast.v1.OptionalMapSetFields._Field field : net.morimekta.test.hazelcast.v1.OptionalMapSetFields._Field.values()) {
            assertThat(actual.has(field), is(true));
            assertThat(expected.has(field), is(true));
            assertThat(newActual.has(field), is(true));
            assertThat(newExpected.has(field), is(true));
        }
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1OptionalMapSetFieldsRand() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapSetFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.OptionalMapSetFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.OptionalMapSetFields expected = generator.nextOptionalMapSetFieldsV1();

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapSetFields actual = readMap.get(key)
                                                                             .build();

        assertThat(expected, is(equalToMessage(actual)));

        net.morimekta.test.hazelcast.v1.OptionalMapSetFields newExpected = actual.mutate()
                                                                                 .setIntegerValueSet(generator.entities.nextIntegerSetMap())
                                                                                 .setDoubleValueSet(generator.entities.nextDoubleSetMap())
                                                                                 .setLongValueSet(generator.entities.nextLongSetMap())
                                                                                 .setShortValueSet(generator.entities.nextShortSetMap())
                                                                                 .setStringValueSet(generator.entities.nextStringSetMap())
                                                                                 .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.OptionalMapSetFields newActual = writeMap.get(key)
                                                                                 .build();

        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1UnionFieldsAll() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.UnionFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.UnionFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.UnionFields expected = generator.nextUnionFieldsV1(true);

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.UnionFields actual = readMap.get(key)
                                                                    .build();

        assertThat(expected, is(actual));

        net.morimekta.test.hazelcast.v1.UnionFields newExpected = actual.mutate()
                                                                        .setIntegerValue(generator.entities.nextInt())
                                                                        .setDoubleValue(generator.entities.nextDouble())
                                                                        .setLongValue(generator.entities.nextLong())
                                                                        .setShortValue(generator.entities.nextShort())
                                                                        .setStringValue(generator.entities.nextString())
                                                                        .setAllFields(AllFields.withByteValue(generator.entities.nextByte()))
                                                                        .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.UnionFields newActual = writeMap.get(key)
                                                                        .build();

        for (net.morimekta.test.hazelcast.v1.UnionFields._Field field : net.morimekta.test.hazelcast.v1.UnionFields._Field.values()) {
            assertThat(actual.has(field), is(true));
            assertThat(expected.has(field), is(true));
            assertThat(newActual.has(field), is(true));
            assertThat(newExpected.has(field), is(true));
        }
        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

    @Test
    public void testVersion1UnionFieldsRand() throws InterruptedException {
        String mapName = nextString();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(getV1Config());
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(getV1Config());

        IMap<String, net.morimekta.test.hazelcast.v1.UnionFields._Builder> writeMap = instance1.getMap(mapName);
        IMap<String, net.morimekta.test.hazelcast.v1.UnionFields._Builder> readMap = instance2.getMap(mapName);

        net.morimekta.test.hazelcast.v1.UnionFields expected = generator.nextUnionFieldsV1();

        String key = nextString();
        writeMap.put(key, expected.mutate());

        net.morimekta.test.hazelcast.v1.UnionFields actual = readMap.get(key)
                                                                    .build();

        assertThat(expected, is(equalToMessage(actual)));

        net.morimekta.test.hazelcast.v1.UnionFields newExpected = actual.mutate()
                                                                        .setIntegerValue(generator.entities.nextInt())
                                                                        .setDoubleValue(generator.entities.nextDouble())
                                                                        .setLongValue(generator.entities.nextLong())
                                                                        .setShortValue(generator.entities.nextShort())
                                                                        .setStringValue(generator.entities.nextString())
                                                                        .setAllFields(AllFields.withByteValue(generator.entities.nextByte()))
                                                                        .build();

        readMap.put(key, newExpected.mutate());

        net.morimekta.test.hazelcast.v1.UnionFields newActual = writeMap.get(key)
                                                                        .build();

        assertThat(newExpected, is(equalToMessage(newActual)));
        assertThat(newExpected.hashCode(), is(newActual.hashCode()));
        assertThat(newExpected, is(newActual));
    }

}
