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
package com.meistermeier.neo4j.toolbelt.renderer;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gerrit Meier
 */
public class RendererTest {

	private static final LocalDate LOCAL_DATE = LocalDate.now();
	private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now();
	private static final LocalTime LOCAL_TIME = LocalTime.now();

	private Renderer renderer = Renderer.INSTANCE;

	@Test
	void convertWithDefaultCollectionName() {
		Map<String, Object> parameterCollection = renderer.toParameters(List.of(createParameterInstance())).asMap();
		assertThat(parameterCollection.keySet())
				.hasSize(1)
				.containsExactly("rows");
	}

	@Test
	void convertWithCustomCollectionName() {
		Map<String, Object> parameterCollection = renderer.toParameters(List.of(createParameterInstance()), "eintraege").asMap();
		assertThat(parameterCollection.keySet())
				.hasSize(1)
				.containsExactly("eintraege");
	}

	@Test
	void convertToDriverMatchingTypes() {
		Map<String, Object> parameters = renderer.toParameters(createParameterInstance()).asMap();

		assertThat(parameters.get("a")).isEqualTo("a1");
		assertThat(parameters.get("b")).isEqualTo(1L);
		assertThat(parameters.get("c")).isEqualTo(true);
		assertThat(parameters.get("d")).isEqualTo(2L);
		assertThat(parameters.get("e")).isEqualTo(3d);
		assertThat(parameters.get("f")).isEqualTo(4d);
		assertThat(parameters.get("g")).isEqualTo(LOCAL_DATE);
		assertThat(parameters.get("h")).isEqualTo(LOCAL_DATE_TIME);
		assertThat(parameters.get("i")).isEqualTo(LOCAL_TIME);
		assertThat(parameters.get("j")).isEqualTo(List.of("1", "2", "3"));
		assertThat(parameters.get("k")).isEqualTo(Map.of("foo", "bar"));
	}

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
