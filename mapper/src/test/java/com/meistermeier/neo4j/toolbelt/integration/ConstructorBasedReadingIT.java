/*
 * Copyright 2022-2023 Gerrit Meier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistermeier.neo4j.toolbelt.integration;

import com.meistermeier.neo4j.toolbelt.mapper.Mapper;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gerrit Meier
 */
@Testcontainers
public class ConstructorBasedReadingIT {

	@Container
	static final Neo4jContainer<?> container = new Neo4jContainer<>("neo4j:5")
			.withoutAuthentication();

	static final Mapper mapper = Mapper.INSTANCE;

	static Driver driver;

	@BeforeAll
	static void setupDriverAndData() {
		driver = GraphDatabase.driver(container.getBoltUrl());
		driver.session().run("CREATE (:Node{a: 'a1', b: 'b1', c: ['a1', 'b1', 'c1']})-[:REL]->(:Dings{a:'relatedRecord'})").consume();
		driver.session().run("CREATE (:Node{a: 'a2', b: 'b2', c: ['a2', 'b2', 'c2']})").consume();
	}

	@Test
	void mapRecordFromNode() {
		try (var session = driver.session()) {
			List<ConversionTargetRecord> result = session.run("MATCH (n:Node{a:'a1'})-->(relatedRecord) return n, [relatedRecord] as relatedRecord")
					.list(mapper.createMapperFor(ConversionTargetRecord.class));

			assertThat(result).hasSize(1);
			ConversionTargetRecord converted = result.get(0);
			assertThat(converted.a()).isEqualTo("a1");
			assertThat(converted.b()).isEqualTo("b1");
			assertThat(converted.c())
					.hasSize(3)
					.containsExactly("a1", "b1", "c1");
			assertThat(converted.relatedRecord())
					.isNotNull()
					.isNotEmpty()
					.extracting("a")
					.containsExactly("relatedRecord");
		}
	}

	@Test
	void mapListOfRecordFromNode() {
		try (var session = driver.session()) {
			List<Iterable<ConversionTargetRecord>> result = session.run("MATCH (n:Node) return collect(n)")
					.list(mapper.createCollectionMapperFor(ConversionTargetRecord.class));

			assertThat(result).hasSize(1);
			Iterable<ConversionTargetRecord> convertedClasses = result.get(0);
			assertThat(convertedClasses)
					.hasSize(2)
					.extracting("a", "b", "c")
					.containsExactlyInAnyOrder(
							Tuple.tuple("a1", "b1", List.of("a1", "b1", "c1")),
							Tuple.tuple("a2", "b2", List.of("a2", "b2", "c2"))
					);
		}
	}

	@Test
	void mapClassFromNode() {
		try (var session = driver.session()) {
			List<ConversionTargetClass> result = session.run("MATCH (n:Node{a:'a1'}) return n")
					.list(mapper.createMapperFor(ConversionTargetClass.class));

			assertThat(result).hasSize(1);
			ConversionTargetClass converted = result.get(0);
			assertThat(converted.a).isEqualTo("a1");
			assertThat(converted.b).isEqualTo("b1");
			assertThat(converted.c)
					.hasSize(3)
					.containsExactly("a1", "b1", "c1");
		}
	}

	@Test
	void mapListOfClassFromNode() {
		try (var session = driver.session()) {
			List<Iterable<ConversionTargetClass>> result = session.run("MATCH (n:Node) return collect(n)")
					.list(mapper.createCollectionMapperFor(ConversionTargetClass.class));

			assertThat(result).hasSize(1);
			Iterable<ConversionTargetClass> convertedClasses = result.get(0);
			assertThat(convertedClasses)
					.hasSize(2)
					.extracting("a", "b", "c")
					.containsExactlyInAnyOrder(
							Tuple.tuple("a1", "b1", List.of("a1", "b1", "c1")),
							Tuple.tuple("a2", "b2", List.of("a2", "b2", "c2"))
					);
		}
	}

	public record ConversionTargetRecord(String a, String b, List<String> c, List<RelatedRecord> relatedRecord) { }

	public record RelatedRecord(String a) {}

	public static class ConversionTargetClass {
		public final String a;
		public final String b;
		public final List<String> c;

		public ConversionTargetClass(String a, String b, List<String> c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}
}
