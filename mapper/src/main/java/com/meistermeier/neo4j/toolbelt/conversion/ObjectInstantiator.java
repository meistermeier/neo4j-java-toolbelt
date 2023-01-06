/*
 * Copyright 2022 Gerrit Meier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistermeier.neo4j.toolbelt.conversion;

import com.meistermeier.neo4j.toolbelt.conversion.Converters;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Instantiates objects from class or records and populates their fields,
 * defined in the constructor parameter list.
 * Will always use the most matching constructor compared by parameter names and returned fields.
 *
 * @author Gerrit Meier
 */
class ObjectInstantiator {

	/**
	 * Core entity instantiation function for class/record based mapping.
	 *
	 * @param entityClass Type to get the instance from.
	 * @param converters  Backreference to global converters for property conversion.
	 * @param <T>         Type to process and return.
	 * @return New populated instance of the defined type.
	 */
	<T> Function<MapAccessor, T> createInstance(Class<T> entityClass, Converters converters) {
		return record -> {
			Constructor<T> instantiatingConstructor = determineConstructor(entityClass, record.asMap());

			Parameter[] parameters = instantiatingConstructor.getParameters();
			Value[] values = new Value[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				Parameter parameter = parameters[i];
				String parameterName = parameter.getName();
				Value value = record.get(parameterName);
				values[i] = value;
			}

			try {
				Object[] rawValues = new Object[values.length];
				for (int i = 0; i < values.length; i++) {
					Value value = values[i];
					rawValues[i] = converters.convert(value, getType(parameters[i]));
				}
				return instantiatingConstructor.newInstance(rawValues);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
					 InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@SuppressWarnings("unchecked")
	/**
	 * Find the constructor with the most matching parameter count.
	 */
	private <T> Constructor<T> determineConstructor(Class<T> clazz, Map<String, Object> propertyValue) {
		Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
		Constructor<T> instantiatingConstructorCandidate = null;
		Set<String> availableProperties = propertyValue.keySet();

		int parameterMatchCount = -1;

		for (Constructor<T> constructor : constructors) {
			Parameter[] constructorParameters = constructor.getParameters();
			// if there is no chance that this parameter match count will be higher, dismiss this candidate
			if (constructorParameters.length < parameterMatchCount) {
				continue;
			}

			List<String> constructorParameterNames = Arrays.stream(constructorParameters)
					.map(Parameter::getName).toList();

			int intersectionAmount = calculateIntersectionAmount(constructorParameterNames, availableProperties);
			if (intersectionAmount > parameterMatchCount) {
				instantiatingConstructorCandidate = constructor;
				parameterMatchCount = intersectionAmount;
			}
		}
		return instantiatingConstructorCandidate;
	}

	private int calculateIntersectionAmount(Collection<String> constructorParameterNames, Collection<String> availableProperties) {
		Collection<String> availablePropertiesCopy = new HashSet<>(availableProperties);
		int existingPropertiesAmount = availablePropertiesCopy.size();
		availablePropertiesCopy.removeAll(constructorParameterNames);
		int leftOverPropertiesAmount = availablePropertiesCopy.size();

		return existingPropertiesAmount - leftOverPropertiesAmount;

	}

	private static Class<?> getType(Parameter parameter) throws ClassNotFoundException {
		if (parameter.getType().isAssignableFrom(Map.class)) {
			return Map.class;
		}
		return parameter.getType().getTypeParameters().length == 0
				? parameter.getType()
				: (Class<?>) ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
	}

}
