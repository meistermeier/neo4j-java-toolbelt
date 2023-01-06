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
package com.meistermeier.neo4j.toolbelt.examples.mapper;

import com.meistermeier.neo4j.toolbelt.mapper.Mapper;
import com.meistermeier.neo4j.toolbelt.examples.shared.Environment;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.function.Function;

public class ClassReadingExample {

	private static final StackWalker walker = StackWalker.getInstance();
	private static final Mapper mapper = Mapper.INSTANCE;

	public static void main(String[] args) {
		Driver driver = Environment.getDriver();

		// node based
		mapSingleRecord(driver);

		mapMultipleRecords(driver);
		mapListOfNodes(driver);
		mapMultipleListsOfNodes(driver);

		// value based
		mapFromMultipleValues(driver);

		driver.close();
	}

	public static void mapSingleRecord(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Pet{name: 'Luna', yearBorn: 2017})").consume();

		try (var session = driver.session()) {
			Record singleNode = session.run("MATCH (p:Pet) return p").single();
			Function<Record, Pet> PetConverter = mapper.createMapperFor(Pet.class);

			Pet pet = PetConverter.apply(singleNode);

			List<Pet> pets = session.run("MATCH (p:Pet) return p")
					.list(PetConverter);

			logOutput(pet);
			logOutput(pets);
		}
	}

	public static void mapMultipleRecords(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Pet{name: 'Luna', yearBorn: 2017})").consume();
		driver.session().run("CREATE (:Pet{name: 'Daphne', yearBorn: 1995})").consume();

		try (var session = driver.session()) {
			List<Pet> pets = session.run("MATCH (p:Pet) return p")
					.list(mapper.createMapperFor(Pet.class));

			logOutput(pets);
		}
	}

	public static void mapListOfNodes(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Pet{name: 'Luna', yearBorn: 2017})").consume();
		driver.session().run("CREATE (:Pet{name: 'Daphne', yearBorn: 1995})").consume();

		try (var session = driver.session()) {
			Record PetCollectionRecord = session.run("MATCH (p:Pet) return collect(p)").single();
			Iterable<Pet> pets = mapper.createCollectionMapperFor(Pet.class).apply(PetCollectionRecord);

			logOutput(pets);
		}
	}

	public static void mapMultipleListsOfNodes(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Pet{name: 'Luna', yearBorn: 2017})").consume();
		driver.session().run("CREATE (:Pet{name: 'Daphne', yearBorn: 1995})").consume();

		try (var session = driver.session()) {
			List<Iterable<Pet>> pets = session.run("MATCH (p:Pet) return collect(p)")
					.list(mapper.createCollectionMapperFor(Pet.class));

			logOutput(pets);
		}
	}

	public static void mapFromMultipleValues(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Pet{name: 'Luna', yearBorn: 2017})").consume();

		try (var session = driver.session()) {
			Record singleNode = session.run("MATCH (p:Pet) return p.name as name, p.yearBorn as yearBorn").single();
			Pet pet = mapper.createMapperFor(Pet.class).apply(singleNode);

			logOutput(pet);
		}
	}

	private static void logOutput(Object debugObject) {
		String calleeMethod = walker.walk(frames -> frames
				.skip(1)
				.findFirst()
				.map(StackWalker.StackFrame::getMethodName)).get();
		System.out.println(calleeMethod);
		System.out.println(debugObject);
	}
}
