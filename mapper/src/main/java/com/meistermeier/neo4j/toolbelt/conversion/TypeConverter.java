package com.meistermeier.neo4j.toolbelt.conversion;

import org.neo4j.driver.Value;

/**
 * Definition of a type converter.
 * An implementation must provide the methods {@code canConvert} and {@code convert}.
 *
 * @author Gerrit Meier
 */
public interface TypeConverter {
	boolean canConvert(Value value, Class<?> type);

	<T> T convert(Value value, Class<T> type);
}
