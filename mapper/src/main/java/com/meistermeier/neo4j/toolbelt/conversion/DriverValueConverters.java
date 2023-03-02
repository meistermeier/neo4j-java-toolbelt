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
package com.meistermeier.neo4j.toolbelt.conversion;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Built-in converter to converter Neo4j Java driver types.
 *
 * @author Gerrit Meier
 */
final class DriverValueConverters {

	private final BiFunction<MapAccessor, TypeMetaData<?>, Object> convertDelegate;

	private static final TypeSystem typeSystem = TypeSystem.getDefault();

	private static final List<DriverValueConverter> BASIC_CONVERSIONS = List.of(
			conversion(Value::asObject, Long.class, long.class),
			conversion(Value::asInt, Integer.class, int.class),
			conversion(Value::asFloat, Float.class, float.class),
			conversion(Value::asDouble, Double.class, double.class),
			conversion(Value::asString, String.class),
			conversion(Value::asBoolean, Boolean.class),
			conversion(Value::asLocalDate, LocalDate.class),
			conversion(Value::asLocalDateTime, LocalDateTime.class),
			conversion(Value::asLocalTime, LocalTime.class),
			conversion(Value::asOffsetDateTime, OffsetDateTime.class),
			conversion(Value::asOffsetTime, OffsetTime.class),
			conversion(Value::asZonedDateTime, ZonedDateTime.class)
	);

	private final List<DriverValueConverter> collectionConversions;

	DriverValueConverters(BiFunction<MapAccessor, TypeMetaData<?>, Object> convertDelegate) {
		this.convertDelegate = convertDelegate;

		// because we need to have the callback for the delegate, we cannot instantiate this static.
		this.collectionConversions = List.of(
				conversionWithTypeMetaData(
						(v, t) -> v.asMap(mapValue -> convertDelegate.apply(mapValue, TypeMetaData.from(String.class, t.genericType()))),
						(v, t) -> typeSystem.MAP().isTypeOf(v) && t.type().isAssignableFrom(Map.class),
						Map.class),
				conversionWithTypeMetaData(
						(v, t) -> v.asList(nestedValue -> convertDelegate.apply(nestedValue, TypeMetaData.from(t.genericType(), null))),
						(v, t) -> typeSystem.LIST().isTypeOf(v),
						List.class)
		);
	}

	record DriverValueConverter(BiFunction<Value, TypeMetaData<?>, Object> readingFunction, Function<Object, Value> writingFunction, BiFunction<Value, TypeMetaData<?>, Boolean> canConvertFunction, Class<?>... types) implements ValueConverter {

		@Override
		public boolean canConvert(Value value, TypeMetaData<?> typeMetaData) {
			return canConvertFunction.apply(value, typeMetaData);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object convert(Value value, TypeMetaData<?> typeMetaData) {
			return readingFunction.apply(value, typeMetaData);
		}

	}

	public boolean canConvert(Object object, TypeMetaData<?> typeMetaData) {
		if (!(object instanceof Value value)) {
			return false;
		}

		if (value.isNull()) {
			return true;
		}

		for (DriverValueConverter basicConversion : BASIC_CONVERSIONS) {
			if (basicConversion.canConvert(value, typeMetaData)) {
				return true;
			}
		}

		for (DriverValueConverter collectionConversion : collectionConversions) {
			if (collectionConversion.canConvert(value, typeMetaData)) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(MapAccessor mapAccessor, TypeMetaData<T> typeMetaData) {
		if (!(mapAccessor instanceof Value value)) {
			return (T) convertDelegate.apply(mapAccessor, typeMetaData);
		}

		// Thinking about pushing this down to every converter so that every type specific converter
		// can handle a null value.
		if (value.isNull()) {
			return null;
		}

		for (DriverValueConverter collectionConversion : collectionConversions) {
			if (collectionConversion.canConvert(value, typeMetaData)) {
				// the cake is a lie
				return (T) collectionConversion.convert(value, typeMetaData);
			}
		}

		for (DriverValueConverter conversion : BASIC_CONVERSIONS) {
			if (conversion.canConvert(value, typeMetaData)) {
				return (T) conversion.convert(value, typeMetaData);
			}
		}

		return (T) convertDelegate.apply(value, typeMetaData);
	}

	private static DriverValueConverter conversion(Function<Value, Object> readingFunction, Class<?>... types) {
		return DriverValueConverterBuilder.forTypes(types).readsWith((v, t) -> readingFunction.apply(v)).writesWith(Values::value).checksWith((v, t) -> Arrays.asList(types).contains(t.type()));
	}

	private static DriverValueConverter conversionWithTypeMetaData(BiFunction<Value, TypeMetaData<?>, Object> readingFunction, BiFunction<Value, TypeMetaData<?>, Boolean> checker, Class<?>... types) {
		return DriverValueConverterBuilder.forTypes(types).readsWith(readingFunction).writesWith(Values::value).checksWith(checker);
	}

	// I'll come to naming hell.
	static final class DriverValueConverterBuilder {
		record DriverValueConverterBuilderForTypes(Class<?>... types) {
			DriverValueConverterBuilderForTypesWithReading readsWith(BiFunction<Value, TypeMetaData<?>, Object> readingFunction) {
				return new DriverValueConverterBuilderForTypesWithReading(readingFunction, types);
			}
		}

		record DriverValueConverterBuilderForTypesWithReadingAndWriting(BiFunction<Value, TypeMetaData<?>, Object> readingFunction, Function<Object, Value> writingFunction, Class<?>... types) {
			DriverValueConverter checksWith(BiFunction<Value, TypeMetaData<?>, Boolean> checker) {
				return new DriverValueConverter(readingFunction, writingFunction, checker, types);
			}
		}

		record DriverValueConverterBuilderForTypesWithReading(BiFunction<Value, TypeMetaData<?>, Object> readingFunction, Class<?>... types) {
			DriverValueConverterBuilderForTypesWithReadingAndWriting writesWith(Function<Object, Value> writingFunction) {
				return new DriverValueConverterBuilderForTypesWithReadingAndWriting(readingFunction, writingFunction, types);
			}
		}
		public static DriverValueConverterBuilderForTypes forTypes(Class<?>... classes) {
			return new DriverValueConverterBuilderForTypes(classes);
		}

	}

}
