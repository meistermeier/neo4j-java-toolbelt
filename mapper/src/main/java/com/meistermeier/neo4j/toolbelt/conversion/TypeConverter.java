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
