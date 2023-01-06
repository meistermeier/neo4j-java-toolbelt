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
import org.neo4j.driver.types.MapAccessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Registry for all converters.
 *
 * @author Gerrit Meier
 */
public final class Converters {

	private final Set<ValueConverter> internalValueConverters = new HashSet<>();
	private final Set<TypeConverter<MapAccessor>> internalTypeConverters = new HashSet<>();

	/**
	 * Convenience constructor with default converters.
	 */
	public Converters() {
		this.internalValueConverters.add(new DriverValueConverter());
		this.internalTypeConverters.add(new EntityConverter(this));
	}

	/**
	 * Convert given value into target type.
	 *
	 * @param <T>                  resulting target type
	 * @param mapAccessor          input value
	 * @param type                 target type
	 * @param genericTypeParameter generic type of the base type, if needed/provided
	 * @return converted value or throws {@link ConversionException} if no suitable converter can be found
	 */
	public <T> T convert(MapAccessor mapAccessor, Class<T> type, Class<?> genericTypeParameter) {
		if (mapAccessor == null) {
			return null;
		}
		if (mapAccessor instanceof Value value) {
			if (value.isNull()) {
				return null;
			}

			for (ValueConverter valueConverter : internalValueConverters) {
				if (valueConverter.canConvert(value, type, genericTypeParameter)) {
					return valueConverter.convert(value, type, genericTypeParameter);
				}
			}
		}
		for (TypeConverter<MapAccessor> typeConverter : internalTypeConverters) {
			if (typeConverter.canConvert(mapAccessor, type, genericTypeParameter)) {
				return typeConverter.convert(mapAccessor, type, genericTypeParameter);
			}
		}

		throw new ConversionException("Cannot convert %s to %s".formatted(mapAccessor, type));
	}

	/**
	 * @param value                value to check for. Can be null.
	 * @param type                 type to check for.
	 * @param genericTypeParameter generic type if type is generic
	 * @return is there at least one converter that supports this type or type / value combination.
	 */
	public boolean canConvertToJava(Value value, Class<?> type, Class<?> genericTypeParameter) {
		for (ValueConverter internalValueConverter : internalValueConverters) {
			if (internalValueConverter.canConvert(value, type, genericTypeParameter)) {
				return true;
			}
		}
		for (TypeConverter<MapAccessor> internalValueConverter : internalTypeConverters) {
			if (internalValueConverter.canConvert(null, type, genericTypeParameter)) {
				return true;
			}
		}
		return false;
	}
}
