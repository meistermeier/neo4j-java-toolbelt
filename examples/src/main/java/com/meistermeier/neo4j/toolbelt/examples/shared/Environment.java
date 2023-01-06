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
package com.meistermeier.neo4j.toolbelt.examples.shared;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Environment {

	public static final Properties PROPERTIES = new Properties();

	static {
		try {
			InputStream propertyFile = Environment.class.getResourceAsStream("/example.properties");
			PROPERTIES.load(propertyFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Driver getDriver() {
		String uri = PROPERTIES.getProperty("neo4j.uri");
		String user = PROPERTIES.getProperty("neo4j.user");
		String password = PROPERTIES.getProperty("neo4j.password");

		return GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}
}
