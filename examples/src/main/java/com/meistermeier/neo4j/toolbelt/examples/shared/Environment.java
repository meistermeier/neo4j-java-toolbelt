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
