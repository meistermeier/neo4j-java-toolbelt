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

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;

import java.util.Map;

/**
 * Entity converter that delegates to the {@link ObjectInstantiator}
 * if it can convert the incoming structure.
 *
 * @author Gerrit Meier
 */
public class EntityConverter implements TypeConverter<MapAccessor> {

	private final TypeSystem typeSystem = TypeSystem.getDefault();
	private final ObjectInstantiator objectInstantiator = new ObjectInstantiator();
	private final Converters converters;

	public EntityConverter(Converters converters) {
		this.converters = converters;
	}

	@Override
	public boolean canConvert(MapAccessor mapAccessor, Class<?> type) {
		if (mapAccessor instanceof Value value) {
			return typeSystem.NODE().isTypeOf(value) || typeSystem.MAP().isTypeOf(value);
		}
		return mapAccessor instanceof Record;
	}

	@Override
	public <T> T convert(MapAccessor record, Class<T> entityClass) {
		Map<String, Object> recordMap = record.asMap();
		if (recordMap.size() == 1) {
			Value value = record.values().iterator().next();
			if (value.type().equals(typeSystem.NODE()) || value.type().equals(typeSystem.MAP())) {
				return objectInstantiator.createInstance(entityClass, converters).apply(value);
			}
		}
		// plain property based result
		return objectInstantiator.createInstance(entityClass, converters).apply(record);
	}
}
