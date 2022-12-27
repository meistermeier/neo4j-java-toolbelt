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

/**
 * Exception to be used by the {@link TypeConverter} implementing converters.
 *
 * @author Gerrit Meier
 */
public class ConversionException extends RuntimeException {

	/**
	 * Default constructor that redirects to the generic {@link RuntimeException}.
	 *
	 * @param message Exception message
	 */
	public ConversionException(String message) {
		super(message);
	}
}
