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

/**
 * Definition of a type converter.
 * An implementation must provide the methods {@code canConvert} and {@code convert}.
 *
 * @author Gerrit Meier
 */
public interface TypeConverter {

	/**
	 * Reports if this converter can convert the given type.
	 *
	 * @param value the Java driver value
	 * @param type  the field type the converter should convert the value to
	 * @return true, if the converter takes responsibility for this type, otherwise false
	 */
	boolean canConvert(Value value, Class<?> type);

	/**
	 * Converts the given driver value into the requested type.
	 *
	 * @param value the Java driver value
	 * @param type  the field type the converter should convert the value to
	 * @param <T>   Expected type of the returned object
	 * @return the converted value object
	 */
	<T> T convert(Value value, Class<T> type);
}
