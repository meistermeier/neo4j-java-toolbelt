/*
 * Copyright 2022-2023 Gerrit Meier
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

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Entity converter that delegates to the {@link ObjectInstantiator}
 * if it can convert the incoming structure.
 *
 * @author Gerrit Meier
 */
final class EntityConverter implements TypeConverter<MapAccessor> {

	private final TypeSystem typeSystem = TypeSystem.getDefault();
	private final ObjectInstantiator objectInstantiator = new ObjectInstantiator();
	private final ConverterRegistry converterRegistry;

	public EntityConverter(ConverterRegistry converterRegistry) {
		this.converterRegistry = converterRegistry;
	}

	@Override
	public boolean canConvert(MapAccessor mapAccessor, TypeMetaData<?> typeMetaData) {
		if (mapAccessor instanceof Value value) {
			return typeSystem.NODE().isTypeOf(value) || typeSystem.MAP().isTypeOf(value) || typeSystem.LIST().isTypeOf(value);
		}
		return mapAccessor instanceof Record;
	}

	@SuppressWarnings("unchecked")
	@Override
	public  Object convert(MapAccessor mapAccessor, TypeMetaData<?> typeMetaData) {
		if (mapAccessor instanceof Value value && typeSystem.LIST().isTypeOf(value)) {
			List<Object> collectionEntities = new ArrayList<>();
			for (Value ding : value.asList(Function.identity())) {
				HeadAndTail headAndTail = HeadAndTail.from(ding, typeSystem);
				Object entity = objectInstantiator.createInstance(typeMetaData.genericType(), headAndTail.head(), headAndTail.tail(), converterRegistry);
				collectionEntities.add(entity);
			}
			// yes, I know that List<T> is not <T> but ¯\_(ツ)_/¯
			return collectionEntities;
		}
		HeadAndTail headAndTail = HeadAndTail.from(mapAccessor, typeSystem);
		return objectInstantiator.createInstance(typeMetaData.type(), headAndTail.head(), headAndTail.tail(), converterRegistry);
	}

	private record HeadAndTail(MapAccessor head, Map<String, MapAccessor> tail) {
		static HeadAndTail from(MapAccessor mapAccessor, TypeSystem typeSystem) {
			if (mapAccessor instanceof Value value && typeSystem.NODE().isTypeOf(value)) {
				return new HeadAndTail(mapAccessor, Map.of());
			}
			for (Value value : mapAccessor.values()) {
				if (typeSystem.NODE().isTypeOf(value)) {
					// maybe there is a "faster" way of getting this values converted into MapAccessor
					Map<String, Value> ding = mapAccessor.asMap(Function.identity());
					var tailValue = ding.entrySet().stream()
							.filter(entryToCheck -> entryToCheck.getValue() != value)
							.collect(Collectors.toMap(Map.Entry::getKey, entry -> (MapAccessor) entry.getValue()));

					return new HeadAndTail(value, tailValue);
				}
			}
			return new HeadAndTail(mapAccessor, Map.of());
		}
	}
}
