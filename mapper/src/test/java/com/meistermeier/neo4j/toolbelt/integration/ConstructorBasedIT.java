package com.meistermeier.neo4j.toolbelt.integration;

import com.meistermeier.neo4j.toolbelt.mapper.Converter;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.types.TypeSystem;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gerrit Meier
 */
@Testcontainers
public class ConstructorBasedIT {

	@Container
	static final Neo4jContainer<?> container = new Neo4jContainer<>("neo4j:5")
			.withoutAuthentication();

	static final Converter converter = Converter.INSTANCE.apply(TypeSystem.getDefault());

	static Driver driver;

	@BeforeAll
	static void setupDriverAndData() {
		driver = GraphDatabase.driver(container.getBoltUrl());
		driver.session().run("CREATE (:Node{a: 'a1', b: 'b1', c: ['a1', 'b1', 'c1']})").consume();
		driver.session().run("CREATE (:Node{a: 'a2', b: 'b2', c: ['a2', 'b2', 'c2']})").consume();
	}

	@Test
	void mapRecordFromNode() {
		try (var session = driver.session()) {
			List<ConversionTargetRecord> result = session.run("MATCH (n:Node{a:'a1'}) return n")
					.list(converter.createConverterFor(ConversionTargetRecord.class));

			assertThat(result).hasSize(1);
			ConversionTargetRecord converted = result.get(0);
			assertThat(converted.a()).isEqualTo("a1");
			assertThat(converted.b()).isEqualTo("b1");
			assertThat(converted.c())
					.hasSize(3)
					.containsExactly("a1", "b1", "c1");
		}
	}

	@Test
	void mapListOfRecordFromNode() {
		try (var session = driver.session()) {
			List<Iterable<ConversionTargetRecord>> result = session.run("MATCH (n:Node) return collect(n)")
					.list(converter.createCollectionConverterFor(ConversionTargetRecord.class));

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
					.list(converter.createConverterFor(ConversionTargetClass.class));

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
					.list(converter.createCollectionConverterFor(ConversionTargetClass.class));

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

	public record ConversionTargetRecord(String a, String b, List<String> c) {
	}

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
