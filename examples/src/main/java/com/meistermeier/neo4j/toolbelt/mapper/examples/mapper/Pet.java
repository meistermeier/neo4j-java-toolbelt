package com.meistermeier.neo4j.toolbelt.mapper.examples.mapper;

/**
 * @author Gerrit Meier
 */
public class Pet {

	final String name;
	final int yearBorn;

	public Pet(String name, int yearBorn) {
		this.name = name;
		this.yearBorn = yearBorn;
	}

	@Override
	public String toString() {
		return "Pet{" +
				"name='" + name + '\'' +
				", yearBorn=" + yearBorn +
				'}';
	}
}
