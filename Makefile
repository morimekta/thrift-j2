VERSION := $(shell cat pom.xml | grep "^    <version>" | sed -e 's:.*<version>::' -e 's:</version>.*::')
THRIFT_VERSION := $(shell cat pom.xml | grep ".*<thrift.version>" | sed -e 's:.*<thrift.version>::' -e 's:</thrift.version>.*::')

compile:
	mvn -Pall net.morimekta.providence:providence-maven-plugin:$(VERSION):compile
	mvn -Dprovidence.main.input=github.com/morimekta/providence \
	    -Pall net.morimekta.providence:providence-maven-plugin:$(VERSION):compile

test-compile:
	mvn -Pall net.morimekta.providence:providence-maven-plugin:$(VERSION):testCompile
	mvn -Dprovidence.gen.rw_binary=false \
	    -Dprovidence.test.input=src/test/no_rw_binary/**/*.thrift \
	    -Pall net.morimekta.providence:providence-maven-plugin:$(VERSION):testCompile

models: compile
	mv providence-core/target/generated-sources/providence/net/morimekta/providence/* \
	   providence-core/src/main/java/net/morimekta/providence

	rm   -rf idl/src/main/java-gen/*
	mkdir -p idl/src/main/java-gen/net/morimekta/providence/model
	mv idl/target/generated-sources/providence/net/morimekta/providence/*.java \
	   idl/src/main/java-gen/net/morimekta/providence
	mv idl/target/generated-sources/providence/net/morimekta/providence/model/*.java \
	   idl/src/main/java-gen/net/morimekta/providence/model

test-models: test-compile
	rm -rf providence-core/src/test/java-gen/*
	mv providence-core/target/generated-test-sources/providence/* \
	   providence-core/src/test/java-gen/
	rm -rf providence-reflect/src/test/java-gen/*
	mv providence-reflect/target/generated-test-sources/providence/* \
	   providence-reflect/src/test/java-gen/

resources:
	mvn clean package -Pit-generator
	cp -R it-generator-java/target/java.jar providence-tools-generator/src/test/resources/generator
	cp -R it-generator-js/target/js.jar     providence-tools-generator/src/test/resources/generator

##################
## -- THRIFT -- ##
##################

%.thrift.done: %.thrift
	$(eval OUT=$(shell dirname $< | sed 's:/thrift:/java-gen:'))
	mkdir -p $(OUT)
	thrift-$(THRIFT_VERSION) --out $(OUT) --gen java:generated_annotations=suppress,private-members,fullcamel $<
	touch $@

THRIFT_FILES=$(wildcard */src/main/thrift/*.thrift)
THRIFT_LOCKS=$(patsubst %.thrift,%.thrift.done, $(THRIFT_FILES))

thrift: $(THRIFT_LOCKS)

TEST_THRIFT_FILES=$(wildcard */src/test/thrift/*.thrift)
TEST_THRIFT_LOCKS=$(patsubst %.thrift,%.thrift.done, $(TEST_THRIFT_FILES))

test-thrift: $(TEST_THRIFT_LOCKS)

clean:
	@rm -rf $(THRIFT_LOCKS) $(TEST_THRIFT_LOCKS)


.PHONY: compile test-compile models test-models thrift js
