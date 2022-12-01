package com.meistermeier.neo4j.toolbelt.mapper.examples.shared;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.io.IOException;
import java.util.Properties;

public class Environment {

	public static final Properties PROPERTIES = new Properties();

	static {
		try {
			PROPERTIES.load(Environment.class.getResourceAsStream("/example.properties"));
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
