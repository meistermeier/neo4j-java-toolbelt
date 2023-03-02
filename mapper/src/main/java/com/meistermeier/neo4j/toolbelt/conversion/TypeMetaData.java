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
 * WIP class to have a wrapper around types and containing types,
 * if there are generics.
 *
 * @author Gerrit Meier
 */
public record TypeMetaData<T>(Class<T> type, Class<?> genericType) {

	/**
	 * Creates a wrapper for type definition and potential generic type of
	 * collection or similar
	 *
	 * @param type			field type
	 * @param genericType	generic type if supported
	 * @return new {@link TypeMetaData} for this type.
	 */
	public static TypeMetaData<?> from(Class<?> type, Class<?> genericType) {
		return new TypeMetaData<>(type, genericType);
	}
}
