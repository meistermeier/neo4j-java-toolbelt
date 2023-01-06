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
package com.meistermeier.neo4j.toolbelt.mapper;

import com.meistermeier.neo4j.toolbelt.conversion.Converters;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;

import java.util.List;
import java.util.function.Function;

/**
 * Class and record mapper.
 * Call {@code Mapper.INSTANCE} to get a prepared instance.
 *
 * @author Gerrit Meier
 */
public class Mapper {

	/**
	 * A prepared {@link Mapper} instance.
	 */
	public final static Mapper INSTANCE = new Mapper();

	private final TypeSystem typeSystem = TypeSystem.getDefault();
	private final Converters converters;

	private Mapper() {
		this.converters = new Converters();
	}

	/**
	 * Create a mapper for the requested type.
	 * Can be reused.
	 *
	 * @param type Type to create the mapping function for.
	 * @param <T>  Type definition
	 * @return Function that is capable of mapping the result into the desired type.
	 */
	public <T> Function<Record, T> createMapperFor(Class<T> type) {
		return record -> mapOne(record, type);
	}

	/**
	 * Create a mapper for a collection of the requested type.
	 * Can be reused.
	 *
	 * @param type Type to create the mapping function for.
	 * @param <T>  Type definition
	 * @return Function that is capable of mapping the result into the desired type.
	 */
	public <T> Function<Record, Iterable<T>> createCollectionMapperFor(Class<T> type) {
		return record -> mapAll(record, type);
	}

	<T> T mapOne(MapAccessor mapAccessor, Class<T> type) {
		return converters.convert(mapAccessor, type);
	}

	private <T> Iterable<T> mapAll(Record record, Class<T> type) {
		if (record.get(0).isNull()) {
			return List.of();
		}
		if (typeSystem.LIST().isTypeOf(record.get(0))) {
			return record.get(0).asList(nestedValue -> mapOne(nestedValue, type));
		}
		return record.values(value -> mapOne(value, type));
	}
}
