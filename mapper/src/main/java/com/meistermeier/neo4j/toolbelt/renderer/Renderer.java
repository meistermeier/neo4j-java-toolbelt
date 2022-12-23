package com.meistermeier.neo4j.toolbelt.renderer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders objects to parameter maps.
 * Call {@code Renderer.INSTANCE} to get a prepared instance.
 *
 * @author Gerrit Meier
 */
public class Renderer {

	/**
	 * A prepared {@link Renderer} instance.
	 */
	public final static Renderer INSTANCE = new Renderer();

	private final ObjectMapper mapper;

	private Renderer() {
		mapper = new ObjectMapper();
		mapper.registerModule(new Neo4jDriverModule());
	}

	/**
	 * Renders the provided object into a map of parameters.
	 * Will use the default name `rows` if the instance is of type {@link Collection}
	 *
	 * @param instance Instance of the object to render.
	 * @param <T>      Type of instance.
	 * @return map of parameters
	 */
	public <T> Value toParameters(T instance) {
		return toParameters(instance, "rows");
	}

	/**
	 * Renders the provided object into a map of parameters.
	 * Will use the provided {@code collectionName} if the instance is of type {@link Collection}
	 *
	 * @param instance       Instance of the object to render.
	 * @param collectionName Name of a collection, if the instance is of type {@link Collection}
	 * @param <T>            Type of instance.
	 * @return map of parameters
	 */
	public <T> Value toParameters(T instance, String collectionName) {

		if (Collection.class.isAssignableFrom(instance.getClass())) {
			return Values.value(Map.of(collectionName, mapper.convertValue(instance, List.class)));
		}

		Map<String, Value> sourceMap = new HashMap<>(Collections.unmodifiableMap(mapper.convertValue(instance, Map.class)));
		return Values.value(sourceMap);
	}
}
