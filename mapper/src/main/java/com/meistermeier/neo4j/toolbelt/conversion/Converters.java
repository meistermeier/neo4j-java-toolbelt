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
package com.meistermeier.neo4j.toolbelt.conversion;

import org.neo4j.driver.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * Registry for all converters.
 *
 * @author Gerrit Meier
 */
public final class Converters implements TypeConverter {

	private final Set<TypeConverter> internalTypeConverters = new HashSet<>();

	/**
	 * Convenience constructor with default converters.
	 */
	public Converters() {
		this.internalTypeConverters.add(new DriverTypeConverter());
	}

	@Override
	public boolean canConvert(Value value, Class<?> type) {
		for (TypeConverter typeConverter : internalTypeConverters) {
			if (typeConverter.canConvert(value, type)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <T> T convert(Value value, Class<T> type) {
		for (TypeConverter typeConverter : internalTypeConverters) {
			if (typeConverter.canConvert(value, type)) {
				return typeConverter.convert(value, type);
			}
		}
		throw new ConversionException("Cannot convert %s to %s".formatted(value, type));
	}
}
