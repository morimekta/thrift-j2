package net.morimekta.providence.config.impl;

import net.morimekta.providence.PMessage;
import net.morimekta.providence.descriptor.PField;
import net.morimekta.providence.util.ProvidenceHelper;
import net.morimekta.providence.util.SimpleTypeRegistry;
import net.morimekta.test.providence.config.Credentials;
import net.morimekta.test.providence.config.Database;
import net.morimekta.test.providence.config.Service;
import net.morimekta.test.providence.config.ServicePort;
import net.morimekta.testing.ResourceUtils;
import net.morimekta.util.Binary;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static net.morimekta.providence.testing.ProvidenceMatchers.equalToMessage;
import static net.morimekta.testing.ExtraMatchers.equalToLines;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IntermediateConfigConverterTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    private IntermediateConfigConverter converter;
    private IntermediateConfigParser intermediateParser;
    private ProvidenceConfigParser providenceParser;
    private SimpleTypeRegistry registry;

    @Before
    public void setUp() {
        registry = new SimpleTypeRegistry();

        converter = new IntermediateConfigConverter(registry, false);
        intermediateParser = new IntermediateConfigParser(registry, false);
        providenceParser = new ProvidenceConfigParser(registry, false);
    }

    @Test
    public void testConvert_simple() throws IOException {
        registry.registerRecursively(Service.kDescriptor);

        File service = tmp.newFile("service.config");
        ResourceUtils.writeContentTo("config.Service {\n" +
                                     "  db {\n" +
                                     "    uri = \"the-uri\"\n" +
                                     "  }\n" +
                                     "  http = {\n" +
                                     "    port = 8080" +
                                     "  }\n" +
                                     "}\n", service);

        File out = tmp.newFile("out.json");
        converter.convertConfig(service.toPath(), out.toPath());

        String contents = new String(readAllBytes(out.toPath()), UTF_8);

        assertThat(contents, equalToLines(
                "{\n" +
                "    \"config.Service\": {\n" +
                "        \"db\": {\n" +
                "            \"uri\": \"the-uri\"\n" +
                "        },\n" +
                "        \"@resolve:http\": \"replace\",\n" +
                "        \"http\": {\n" +
                "            \"port\": 8080\n" +
                "        }\n" +
                "    }\n" +
                "}\n"));

        Service orig = Service.builder()
                              .setDb(Database.builder()
                                             .setUri("other")
                                             .setDriver("the.Driver")
                                             .build())
                              .setHttp(ServicePort.builder()
                                                  .setPort((short) 80)
                                                  .setContext("/foo")
                                                  .build())
                              .build();


        Service fromConfig = providenceParser.parseConfig(service.toPath(), orig).first;
        Service fromIntermediate = intermediateParser.parseConfig(out.toPath(), orig);

        assertThat(fromIntermediate, is(fromConfig));
    }

    @Test
    public void testIntermediateEquivalence() throws IOException {
        File base = ResourceUtils.copyResourceTo("/net/morimekta/providence/config/intermediate/base_service.config", tmp.getRoot());
        File prod = ResourceUtils.copyResourceTo("/net/morimekta/providence/config/intermediate/prod.config", tmp.getRoot());
        ResourceUtils.copyResourceTo("/net/morimekta/providence/config/intermediate/prod_db.config", tmp.getRoot());
        File stage = ResourceUtils.copyResourceTo("/net/morimekta/providence/config/intermediate/stage.config", tmp.getRoot());
        ResourceUtils.copyResourceTo("/net/morimekta/providence/config/intermediate/stage_db.config", tmp.getRoot());

        registry.registerRecursively(Service.kDescriptor);
        assertIntermediateConfig(service("{\n" +
                                         "  name = \"prod\"\n" +
                                         "  http = {\n" +
                                         "    port = 8080\n" +
                                         "    context = \"/app\"\n" +
                                         "    signature_keys = {\n" +
                                         "      \"app1\": b64(AAAAAAAAAAAAAAAAAAAA)\n" +
                                         "      \"app2\": b64(VGVzdCBPYXV0aCBLZXkK)\n" +
                                         "    }\n" +
                                         "    signature_override_keys = [\n" +
                                         "      \"really_something\"" +
                                         "    ]\n" +
                                         "  }\n" +
                                         "  admin = {\n" +
                                         "    port = 8088\n" +
                                         "    oauth_token_key = b64(AAAAAAAAAAAAAAAAAAAA)\n" +
                                         "  }\n" +
                                         "  db = {\n" +
                                         "    uri = \"jdbc:mysql:db01:1364/my_db\"\n" +
                                         "    driver = \"org.mysql.Driver\"\n" +
                                         "    credentials = {\n" +
                                         "      username = \"dbuser\"\n" +
                                         "      password = \"DbP4s5w0rD\"\n" +
                                         "    }\n" +
                                         "  }\n" +
                                         "  enabled = true\n" +
                                         "  valid_until = 1234567890\n" +
                                         "}\n"),
                                base, prod);
        assertIntermediateConfig(service("{\n" +
                                         "  name = \"stage\"\n" +
                                         "  http = {\n" +
                                         "    port = 8080\n" +
                                         "    context = \"/app\"\n" +
                                         "    signature_keys = {\n" +
                                         "      \"app1\": b64(AAAAAAAAAAAAAAAAAAAA)\n" +
                                         "    }\n" +
                                         "    signature_override_keys = [\n" +
                                         "      \"not_really_app_1\"" +
                                         "    ]\n" +
                                         "  }\n" +
                                         "  db = {\n" +
                                         "    uri = \"jdbc:h2:localhost:mem\"\n" +
                                         "    credentials = {\n" +
                                         "      username = \"myuser\"\n" +
                                         "      password = \"MyP4s5w0rd\"\n" +
                                         "    }\n" +
                                         "  }\n" +
                                         "}\n"),
                                 base, stage);
    }

    private <M extends PMessage<M,F>, F extends PField>
    void assertIntermediateConfig(M expected,
                                  File... files) throws IOException {
        M config = null;
        M intermediate = null;
        for (File cfg : files) {
            config = providenceParser.parseConfig(cfg.toPath(), config).first;

            File json = new File(tmp.getRoot(),
                                 cfg.getName()
                                    .replaceAll("[.][^.]*$", ".json"));
            if (!json.exists()) {
                converter.convertConfig(cfg.toPath(), json.toPath());
            }

            intermediate = intermediateParser.parseConfig(json.toPath(), intermediate);
        }

        assertThat(config,       is(equalToMessage(expected)));
        assertThat(intermediate, is(equalToMessage(expected)));
    }

    Service service(String config) {
        return ProvidenceHelper.parseDebugString(config, Service.kDescriptor);
    }
}
