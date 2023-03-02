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

import org.neo4j.driver.types.MapAccessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Registry for all converters.
 *
 * @author Gerrit Meier
 */
public final class ConverterRegistry {

	private final Set<DriverValueConverters> internalValueConverters = new HashSet<>();
	private final Set<TypeConverter<MapAccessor>> internalTypeConverters = new HashSet<>();
	private final Set<TypeConverter<? extends MapAccessor>> customConverters = new HashSet<>();

	/**
	 * Convenience constructor with default converters.
	 */
	public ConverterRegistry() {
		this.internalValueConverters.add(new DriverValueConverters((value, typeMetaData) -> convert(value, typeMetaData.type(), typeMetaData.genericType())));
		this.internalTypeConverters.add(new EntityConverter(this));
	}

	private ConverterRegistry(TypeConverter<? extends MapAccessor> customConverter) {
		customConverters.add(customConverter);
		this.internalValueConverters.add(new DriverValueConverters((value, typeMetaData) -> convert(value, typeMetaData.type(), typeMetaData.genericType())));
		this.internalTypeConverters.add(new EntityConverter(this));
	}

	public ConverterRegistry addCustomConverter(TypeConverter<? extends MapAccessor> customConverter) {
		return new ConverterRegistry(customConverter);
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
	@SuppressWarnings("unchecked")
	public <T> T convert(MapAccessor mapAccessor, Class<T> type, Class<?> genericTypeParameter) {
		TypeMetaData<?> typeMetaData = TypeMetaData.from(type, genericTypeParameter);
		if (mapAccessor == null) {
			return null;
		}

		for (TypeConverter<? extends MapAccessor> customTypeConverter : customConverters) {
			if (customTypeConverter.canConvert(mapAccessor, typeMetaData)) {
				return (T) customTypeConverter.convert(mapAccessor, typeMetaData);
			}
		}

		for (DriverValueConverters valueConverter : internalValueConverters) {
			if (valueConverter.canConvert(mapAccessor, typeMetaData)) {
				return (T) valueConverter.convert(mapAccessor, typeMetaData);
			}
		}

		for (TypeConverter<MapAccessor> typeConverter : internalTypeConverters) {
			if (typeConverter.canConvert(mapAccessor, typeMetaData)) {
				return (T) typeConverter.convert(mapAccessor, typeMetaData);
			}
		}

		throw new ConversionException("Cannot convert %s to %s".formatted(mapAccessor, type));
	}

}
