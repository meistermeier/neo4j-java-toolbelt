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
