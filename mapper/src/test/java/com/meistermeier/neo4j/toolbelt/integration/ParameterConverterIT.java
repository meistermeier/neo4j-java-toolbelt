/*
 * Copyright 2022 Gerrit Meier
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

import com.meistermeier.neo4j.toolbelt.renderer.Renderer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Value;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gerrit Meier
 */
@Testcontainers
public class ParameterConverterIT {

	@Container
	static final Neo4jContainer<?> container = new Neo4jContainer<>("neo4j:5")
			.withoutAuthentication();

	static final Renderer renderer = Renderer.INSTANCE;
	private static final LocalDate LOCAL_DATE = LocalDate.now();
	private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now();
	private static final LocalTime LOCAL_TIME = LocalTime.now();

	static Driver driver;

	@BeforeAll
	static void setupDriverAndData() {
		driver = GraphDatabase.driver(container.getBoltUrl());
	}

	@Test
	void provideStringTypeParameters() {
		try (var session = driver.session()) {
			String result = session.run("RETURN $a as a",
							renderer.toParameters(createParameterInstance()))
						.single().get("a").asString();

			assertThat(result).isEqualTo("a1");
		}
	}

	@Test
	void provideListTypeParameters() {
		try (var session = driver.session()) {
			List<String> result = session.run("RETURN $j as j",
							renderer.toParameters(createParameterInstance()))
						.single().get("j").asList(Value::asString);

			assertThat(result).containsExactly("1", "2", "3");
		}
	}

	@Test
	void provideDefaultNamedListOfParameters() {
		try (var session = driver.session()) {
			List<String> result = session.run("UNWIND $rows as row RETURN row.a as a",
							renderer.toParameters(createParameterInstances()))
						.list(record -> record.get("a").asString());

			assertThat(result).containsExactly("a1", "a2");
		}
	}

	@Test
	void provideCustomNamedListOfParameters() {
		try (var session = driver.session()) {
			List<String> result = session.run("UNWIND $entries as row RETURN row.a as a",
							renderer.toParameters(createParameterInstances(), "entries"))
						.list(record -> record.get("a").asString());

			assertThat(result).containsExactly("a1", "a2");
		}
	}

	@Test
	void provideDriverOfParameterTypes() {
		try (var session = driver.session()) {
			var resultMap = session.run("UNWIND $entries as row RETURN row as a",
							renderer.toParameters(List.of(createParameterInstance()), "entries"))
						.single();
			var result = resultMap.get("a");

			assertThat(result.get("a").asString()).isEqualTo("a1");
			assertThat(result.get("b").asLong()).isEqualTo(1L);
			assertThat(result.get("c").asBoolean()).isEqualTo(true);
			assertThat(result.get("d").asInt()).isEqualTo(2);
			assertThat(result.get("e").asFloat()).isEqualTo(3f);
			assertThat(result.get("f").asDouble()).isEqualTo(4d);
			assertThat(result.get("g").asLocalDate()).isEqualTo(LOCAL_DATE);
			assertThat(result.get("h").asLocalDateTime()).isEqualTo(LOCAL_DATE_TIME);
			assertThat(result.get("i").asLocalTime()).isEqualTo(LOCAL_TIME);
			assertThat(result.get("j").asList()).isEqualTo(List.of("1", "2", "3"));
		}
	}

	@NotNull
	private static List<ParameterClass> createParameterInstances() {
		return List.of(createParameterInstance(),
				new ParameterClass("a2", 10L, true, 20, 30f, 40d, LOCAL_DATE, LOCAL_DATE_TIME, LOCAL_TIME, List.of("4", "5", "6"), Map.of("x", "y")));
	}

	@NotNull
	private static ParameterClass createParameterInstance() {
		return new ParameterClass(
				"a1",
				1L,
				true,
				2,
				3f,
				4d,
				LOCAL_DATE,
				LOCAL_DATE_TIME,
				LOCAL_TIME,
				List.of("1", "2", "3"),
				Map.of("foo", "bar")
		);
	}

	record ParameterClass(String a, long b, boolean c, int d, float e, double f, LocalDate g,
						  LocalDateTime h, LocalTime i, List<String> j, Map<String, Object> k) {
	}
}
