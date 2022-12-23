package com.meistermeier.neo4j.toolbelt.conversion;

import org.neo4j.driver.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * Registry for all converters.
 *
 * @author Gerrit Meier
 */
public final class Converters {

	private final Set<TypeConverter> internalTypeConverters = new HashSet<>();

	public Converters() {
		this.internalTypeConverters.add(new DriverTypeConverter());
	}

	public boolean canConvert(Value value, Class<?> type) {
		for (TypeConverter typeConverter : internalTypeConverters) {
			if (typeConverter.canConvert(value, type)) {
				return true;
			}
		}
		return false;
	}

	public <T> T convert(Value value, Class<T> type) {
		for (TypeConverter typeConverter : internalTypeConverters) {
			if (typeConverter.canConvert(value, type)) {
				return typeConverter.convert(value, type);
			}
		}
		throw new ConversionException("Cannot convert %s to %s".formatted(value, type));
	}
}
