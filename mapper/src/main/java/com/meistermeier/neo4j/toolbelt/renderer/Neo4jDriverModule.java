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
package com.meistermeier.neo4j.toolbelt.renderer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Adds some conversion for Jackson to the {@code ObjectMapper}
 *
 * @author Gerrit Meier
 */
class Neo4jDriverModule extends SimpleModule {

	Neo4jDriverModule() {
		addSerializer(Integer.class, new Neo4jTypeSerializer<>());
		addSerializer(Double.class, new Neo4jTypeSerializer<>());
		addSerializer(Float.class, new Neo4jTypeSerializer<>());
		addSerializer(Long.class, new Neo4jTypeSerializer<>());
		addSerializer(Boolean.class, new Neo4jTypeSerializer<>());
		addSerializer(String.class, new Neo4jTypeSerializer<>());
		addSerializer(LocalDate.class, new Neo4jTypeSerializer<>());
		addSerializer(LocalDateTime.class, new Neo4jTypeSerializer<>());
		addSerializer(LocalTime.class, new Neo4jTypeSerializer<>());
	}

	private class Neo4jTypeSerializer<T> extends JsonSerializer<T> {

		@Override
		public void serialize(T value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			Value driverValue = Values.value(value);
			jsonGenerator.writeEmbeddedObject(driverValue);
		}
	}
}
