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

	/**
	 * A prepared {@link Mapper} instance.
	 */
	public final static Mapper INSTANCE = new Mapper();

	private final ObjectInstantiator objectInstantiator = new ObjectInstantiator();
	private final TypeSystem typeSystem = TypeSystem.getDefault();
	private final Converters converters;

	private Mapper() {
		this.converters = new Converters();
	}

	/**
	 * Create a mapper for the requested type.
	 * Can be reused.
	 *
	 * @param type Type to create the mapping function for.
	 * @param <T>  Type definition
	 * @return Function that is capable of mapping the result into the desired type.
	 */
	public <T> Function<Record, T> createMapperFor(Class<T> type) {
		return record -> mapOne(record, type);
	}

	/**
	 * Create a mapper for a collection of the requested type.
	 * Can be reused.
	 *
	 * @param type Type to create the mapping function for.
	 * @param <T>  Type definition
	 * @return Function that is capable of mapping the result into the desired type.
	 */
	public <T> Function<Record, Iterable<T>> createCollectionMapperFor(Class<T> type) {
		return record -> mapAll(record, type);
	}

	<T> T mapOne(MapAccessor mapAccessor, Class<T> type) {
		if (mapAccessor instanceof Value value) {
			if (converters.canConvert(value, type)) {
				return converters.convert(value, type);
			}
		}
		return (T) objectInstantiator.createInstance(type, typeSystem, this::mapOne).apply(mapAccessor);
	}

	private <T> Iterable<T> mapAll(Record record, Class<T> type) {
		if (record.get(0).isNull()) {
			return List.of();
		}
		if (typeSystem.LIST().isTypeOf(record.get(0))) {
			return record.get(0).asList(nestedValue -> mapOne(nestedValue, type));
		}
		return record.values(value -> mapOne(value, type));
	}
}
