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
import org.neo4j.driver.types.Type;
import org.neo4j.driver.types.TypeSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Built-in converter to converter Neo4j Java driver types.
 *
 * @author Gerrit Meier
 */
class DriverValueConverter implements ValueConverter {

	private static final TypeSystem typeSystem = TypeSystem.getDefault();
	private static final List<Type> SUPPORTED_SOURCE_VALUES_TYPES = List.of(typeSystem.LIST(), typeSystem.NULL());
	private static final Map<Type, ?> SOURCE_VALUE_TARGET_COMBINATION = Map.of(typeSystem.MAP(), Map.class);

	private static final Map<Class<?>, DriverTypeConversion> BASIC_CONVERSIONS = Map.of(
			Long.class, conversion(Value::asObject, Long.class),
			long.class, conversion(Value::asLong, long.class),
			Integer.class, conversion(Value::asInt, Integer.class),
			int.class, conversion(Value::asInt, int.class),
			Float.class, conversion(Value::asFloat, Float.class),
			float.class, conversion(Value::asFloat, float.class),
			Double.class, conversion(Value::asDouble, Double.class),
			double.class, conversion(Value::asDouble, double.class),
			String.class, conversion(Value::asString, String.class),
			Boolean.class, conversion(Value::asBoolean, Boolean.class)
	);

	private static final Map<Class<?>, DriverTypeConversion> DATE_TIME_CONVERSIONS = Map.of(
			LocalDate.class, conversion(Value::asLocalDate, LocalDate.class),
			LocalDateTime.class, conversion(Value::asLocalDateTime, LocalDateTime.class),
			LocalTime.class, conversion(Value::asLocalTime, LocalTime.class),
			OffsetDateTime.class, conversion(Value::asOffsetDateTime, OffsetDateTime.class),
			OffsetTime.class, conversion(Value::asOffsetTime, OffsetTime.class),
			ZonedDateTime.class, conversion(Value::asZonedDateTime, ZonedDateTime.class)
	);

	@Override
	public boolean canConvert(Value value, Class<?> type) {
		return BASIC_CONVERSIONS.containsKey(type)
				|| DATE_TIME_CONVERSIONS.containsKey(type)
				|| SUPPORTED_SOURCE_VALUES_TYPES.contains(value.type())
				|| (SOURCE_VALUE_TARGET_COMBINATION.containsKey(value.type()) && SOURCE_VALUE_TARGET_COMBINATION.containsValue(type));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Value value, Class<T> type) {
		if (value.isNull()) {
			return null;
		}
		if (typeSystem.MAP().isTypeOf(value)) {
			if (type.isAssignableFrom(Map.class)) {
				// convert into simple map
				return (T) value.asMap(mapValue -> convert(mapValue, String.class));
			}
		}

		if (!type.isArray() && typeSystem.LIST().isTypeOf(value)) {
			return (T) value.asList(nestedValue -> convert(nestedValue, type));
		}
		for (DriverTypeConversion conversion : BASIC_CONVERSIONS.values()) {
			if (conversion.predicate.test(type)) {
				return (T) conversion.conversionFunction.apply(value);
			}
		}
		for (DriverTypeConversion conversion : DATE_TIME_CONVERSIONS.values()) {
			if (conversion.predicate.test(type)) {
				return (T) conversion.conversionFunction.apply(value);
			}
		}
		if (type.isArray() && typeSystem.LIST().isTypeOf(value)) {
			return (T) value.asList(nestedValue -> convert(nestedValue, type.getComponentType()));
		}
		throw new ConversionException("Cannot convert %s to %s".formatted(value, type));
	}

	private static DriverTypeConversion conversion(Function<Value, Object> conversionFunction, Class<?> type) {
		return new DriverTypeConversion(conversionFunction, type);
	}

	private static class DriverTypeConversion {

		final Predicate<Class<?>> predicate;
		final Function<Value, Object> conversionFunction;

		private DriverTypeConversion(Function<Value, Object> conversionFunction, Class<?> type) {
			this.predicate = (typeToCheck) -> typeToCheck.isAssignableFrom(type);
			this.conversionFunction = conversionFunction;
		}
	}
}
