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

	public final static Renderer INSTANCE = new Renderer();

	private final ObjectMapper mapper;

	private Renderer() {
		mapper = new ObjectMapper();
		mapper.registerModule(new Neo4jDriverModule());
	}

	public <T> Value toParameters(T instance) {
		return toParameters(instance, "rows");
	}

	public <T> Value toParameters(T instance, String collectionName) {

		if (Collection.class.isAssignableFrom(instance.getClass())) {
			return Values.value(Map.of(collectionName, mapper.convertValue(instance, List.class)));
		}

		Map<String, Value> sourceMap = new HashMap<>(Collections.unmodifiableMap(mapper.convertValue(instance, Map.class)));
		return Values.value(sourceMap);
	}
}
