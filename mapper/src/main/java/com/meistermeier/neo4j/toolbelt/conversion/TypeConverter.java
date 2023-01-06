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

/**
 * Definition of a type converter.
 * An implementation must provide the methods {@code canConvert} and {@code convert}.
 *
 * @author Gerrit Meier
 */
public interface TypeConverter<T> {

	/**
	 * Reports if this converter can convert the given type.
	 *
	 * @param value                the value to convert
	 * @param type                 the field type the converter should convert the value to
	 * @param genericTypeParameter generic type of the type, if needed/provided
	 * @return true, if the converter takes responsibility for this type, otherwise false
	 */
	boolean canConvert(T value, Class<?> type, Class<?> genericTypeParameter);

	/**
	 * Converts the given driver value into the requested type.
	 *
	 * @param <X>                  Expected type of the returned object
	 * @param value                the value to convert
	 * @param type                 the field type the converter should convert the value to
	 * @param genericTypeParameter generic type of the type, if needed/provided
	 * @return the converted value object
	 */
	<X> X convert(T value, Class<X> type, Class<?> genericTypeParameter);
}
