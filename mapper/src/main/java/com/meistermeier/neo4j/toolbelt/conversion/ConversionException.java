package com.meistermeier.neo4j.toolbelt.conversion;

/**
 * Exception to be used by the {@link TypeConverter} implementing converters.
 *
 * @author Gerrit Meier
 */
public class ConversionException extends RuntimeException {

	public ConversionException(String message) {
		super(message);
	}
}
