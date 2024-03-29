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

/**
 * Definition of a type converter.
 * An implementation must provide the methods {@code canConvert} and {@code convert}.
 * Currently, this converter definition is focused on the result mapping.
 * As a consequence the type parameter cannot get lower than {@link MapAccessor}.
 *
 * @author Gerrit Meier
 */
public interface TypeConverter<T extends MapAccessor> {

	/**
	 * Reports if this converter can convert the given type.
	 *
	 * @param value        the value to convert
	 * @param typeMetaData the target field type
	 * @return true, if the converter takes responsibility for this type, otherwise false
	 */
	boolean canConvert(MapAccessor value, TypeMetaData<?> typeMetaData);

	/**
	 * Converts the given driver value into the requested type.
	 *
	 * @param value        the value to convert
	 * @param typeMetaData the target field type
	 * @return the converted value object
	 */
	Object convert(MapAccessor value, TypeMetaData<?> typeMetaData);
}
