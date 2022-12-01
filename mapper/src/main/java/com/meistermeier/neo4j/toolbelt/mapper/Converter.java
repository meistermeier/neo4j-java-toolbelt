package com.meistermeier.neo4j.toolbelt.mapper;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

public class Converter {

	public static Function<TypeSystem, Converter> INSTANCE = Converter::new;

	private final ObjectInstantiator objectInstantiator = new ObjectInstantiator();
	private final TypeSystem typeSystem;

	Converter(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public <T> Function<Record, T> createConverterFor(Class<T> aClass) {
		return record -> convertOne(record, aClass);
	}

	public <T> Function<Record, Iterable<T>> createCollectionConverterFor(Class<T> aClass) {
		return record -> convertAll(record, aClass);
	}

	<T> T convertOne(MapAccessor mapAccessor, Class<T> type) {
		// todo custom converters from service loader or similar

		if (mapAccessor instanceof Value value) {
			if (value.isNull()) {
				return null;
			}
			if (!type.isArray() && typeSystem.LIST().isTypeOf(value)) {
				return (T) value.asList(nestedValue -> convertOne(nestedValue, type));
			}
			if (type.isAssignableFrom(Long.class)) {
				return (T) (Long) value.asLong();
			}
			if (type.isAssignableFrom(String.class)) {
				return (T) value.asString();
			}
			if (type.isAssignableFrom(Boolean.class)) {
				return (T) (Boolean) value.asBoolean();
			}
			if (type.isAssignableFrom(Integer.class)) {
				return (T) (Integer) value.asInt();
			}
			if (type.isAssignableFrom(Float.class)) {
				return (T) (Float) value.asFloat();
			}
			if (type.isAssignableFrom(Double.class)) {
				return (T) (Double) value.asDouble();
			}
			if (type.isAssignableFrom(LocalDate.class)) {
				return (T) value.asLocalDate();
			}
			if (type.isAssignableFrom(LocalDateTime.class)) {
				return (T) value.asLocalDateTime();
			}
			if (type.isAssignableFrom(LocalTime.class)) {
				return (T) value.asLocalTime();
			}
			if (type.isArray() && typeSystem.LIST().isTypeOf(value)) {
				return (T) value.asList(nestedValue -> convertOne(nestedValue, type.getComponentType()));
			}
		}
		return (T) objectInstantiator.createInstance(type, typeSystem, this::convertOne).apply(mapAccessor);
	}

	private <T> Iterable<T> convertAll(Record record, Class<T> type) {
		if (record.get(0).isNull()) {
			return List.of();
		}
		if (typeSystem.LIST().isTypeOf(record.get(0))) {
			return record.get(0).asList(nestedValue -> convertOne(nestedValue, type));
		}
		return record.values(value -> convertOne(value, type));
	}

}
