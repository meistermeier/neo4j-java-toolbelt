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
package com.meistermeier.neo4j.toolbelt.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.InternalRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

	Mapper mapper = Mapper.INSTANCE;

	@Nested
	@DisplayName("Record mapping")
	class RecordMapping {

		private final Function<Record, ConversionTargetRecord> recordMapper = mapper.createMapperFor(ConversionTargetRecord.class);

		@Test
		void convertOneFieldToRecord() {
			var record = asRecord(Map.of("a", "a"));
			ConversionTargetRecord conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isEqualTo("a");
			assertThat(conversionTarget.b).isNull();
		}

		@Test
		void convertMultipleFieldsToRecord() {
			var record = asRecord(Map.of("a", "a", "b", "b"));

			ConversionTargetRecord conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isEqualTo("a");
			assertThat(conversionTarget.b).isEqualTo("b");
		}

		@Test
		void convertCollectionFieldToRecord() {
			var record = asRecord(Map.of("a", "a", "b", "b", "c", Values.value("a", "b", "c")));

			ConversionTargetRecord conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isEqualTo("a");
			assertThat(conversionTarget.b).isEqualTo("b");
			assertThat(conversionTarget.c)
					.hasSize(3)
					.containsExactly("a", "b", "c");
		}

		@Test
		void convertMapFieldToRecord() {
			var record = asRecord(Map.of("d", Map.of("something", "d1")));

			ConversionTargetRecord conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.d)
					.hasSize(1)
					.containsEntry("something", "d1");
		}

		@Test
		void convertUnorderedFieldsToRecord() {
			var record = asRecord(Map.of("c", Values.value("a", "b", "c"), "a", "a", "b", "b", "d", Map.of("something", "d1")));

			ConversionTargetRecord conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isEqualTo("a");
			assertThat(conversionTarget.b).isEqualTo("b");
			assertThat(conversionTarget.c)
					.hasSize(3)
					.containsExactly("a", "b", "c");
			assertThat(conversionTarget.d)
					.hasSize(1)
					.containsEntry("something", "d1");
		}

		@Test
		void ignoreUnknownValues() {
			var record = asRecord(Map.of("aa", "a", "bb", "b", "cc", Values.value("a", "b", "c"), "dd", Map.of("something", "d1")));

			ConversionTargetRecord conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isNull();
			assertThat(conversionTarget.b).isNull();
			assertThat(conversionTarget.c).isNull();
			assertThat(conversionTarget.d).isNull();
		}

		public record ConversionTargetRecord(String a, String b, List<String> c, Map<String, String> d) { }
	}

	@Nested
	@DisplayName("Class mapping")
	class ClassMapping {

		private final Function<Record, ConversionTargetClass> recordMapper = mapper.createMapperFor(ConversionTargetClass.class);
		
		@Test
		void convertOneFieldToClass() {
			var record = asRecord(Map.of("a", "a"));

			ConversionTargetClass conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isEqualTo("a");
			assertThat(conversionTarget.b).isNull();
		}

		@Test
		void convertMultipleFieldsToClass() {
			var record = asRecord(Map.of("a", "a", "b", "b"));

			ConversionTargetClass conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isEqualTo("a");
			assertThat(conversionTarget.b).isEqualTo("b");
		}

		@Test
		void convertCollectionFieldToClass() {
			var record = asRecord(Map.of("a", "a", "b", "b", "c", Values.value("a", "b", "c")));

			ConversionTargetClass conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isEqualTo("a");
			assertThat(conversionTarget.b).isEqualTo("b");
			assertThat(conversionTarget.c)
					.hasSize(3)
					.containsExactly("a", "b", "c");
		}

		@Test
		void convertMapFieldToClass() {
			var record = asRecord(Map.of("d", Map.of("something", "d1")));

			ConversionTargetClass conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.d)
					.hasSize(1)
					.containsEntry("something", "d1");
		}

		@Test
		void convertUnorderedFieldsToClass() {
			var record = asRecord(Map.of("c", Values.value("a", "b", "c"), "a", "a", "b", "b", "d", Map.of("something", "d1")));

			ConversionTargetClass conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isEqualTo("a");
			assertThat(conversionTarget.b).isEqualTo("b");
			assertThat(conversionTarget.c)
					.hasSize(3)
					.containsExactly("a", "b", "c");
			assertThat(conversionTarget.d)
					.hasSize(1)
					.containsEntry("something", "d1");
		}

		@Test
		void ignoreUnknownValues() {
			var record = asRecord(Map.of("aa", "a", "bb", "b", "cc", Values.value("a", "b", "c"), "dd", Map.of("something", "d1")));

			ConversionTargetClass conversionTarget = recordMapper.apply(record);
			assertThat(conversionTarget.a).isNull();
			assertThat(conversionTarget.b).isNull();
			assertThat(conversionTarget.c).isNull();
			assertThat(conversionTarget.d).isNull();
		}

		public static class ConversionTargetClass {
			public final String a;
			public final String b;
			public final List<String> c;
			public final Map<String, String> d;

			public ConversionTargetClass(String a, String b, List<String> c, Map<String, String> d) {
				this.a = a;
				this.b = b;
				this.c = c;
				this.d = d;
			}
		}
	}

	static Record asRecord(Map<String, Object> values) {
		List<String> keys = new ArrayList<>();
		List<Value> recordValues = new ArrayList<>();
		values.entrySet().stream()
				.forEach(entry -> {
					keys.add(entry.getKey());
					recordValues.add(Values.value(entry.getValue()));
				});
		return new InternalRecord(keys, recordValues.toArray(new Value[0]));
	}
}
