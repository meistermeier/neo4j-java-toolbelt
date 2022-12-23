package com.meistermeier.neo4j.toolbelt.mapper;

import com.meistermeier.neo4j.toolbelt.conversion.Converters;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;

import java.util.List;
import java.util.function.Function;

/**
 * Class and record mapper.
 * Call {@code Mapper.INSTANCE} to get a prepared instance.
 *
 * @author Gerrit Meier
 */
public class Mapper {

	public final static Mapper INSTANCE = new Mapper();

	private final ObjectInstantiator objectInstantiator = new ObjectInstantiator();
	private final TypeSystem typeSystem = TypeSystem.getDefault();
	private final Converters converters;

	private Mapper() {
		this.converters = new Converters();
	}

	public <T> Function<Record, T> createConverterFor(Class<T> aClass) {
		return record -> convertOne(record, aClass);
	}

	public <T> Function<Record, Iterable<T>> createCollectionConverterFor(Class<T> aClass) {
		return record -> convertAll(record, aClass);
	}

	<T> T convertOne(MapAccessor mapAccessor, Class<T> type) {
 		if (mapAccessor instanceof Value value) {
			if (converters.canConvert(value, type)) {
				return converters.convert(value, type);
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
