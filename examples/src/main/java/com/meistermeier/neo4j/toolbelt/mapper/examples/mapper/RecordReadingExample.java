package com.meistermeier.neo4j.toolbelt.mapper.examples.mapper;

import com.meistermeier.neo4j.toolbelt.mapper.Mapper;
import com.meistermeier.neo4j.toolbelt.mapper.examples.shared.Environment;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.function.Function;

public class RecordReadingExample {

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
		mapFromValueMap(driver);
		mapFromMultipleValues(driver);

		driver.close();
	}

	public static void mapSingleRecord(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();

		try (var session = driver.session()) {
			Record singleNode = session.run("MATCH (p:Person) return p").single();
			Function<Record, Person> personConverter = mapper.createConverterFor(Person.class);

			Person person = personConverter.apply(singleNode);

			List<Person> people = session.run("MATCH (p:Person) return p")
					.list(personConverter);

			logOutput(person);
			logOutput(people);
		}
	}

	public static void mapMultipleRecords(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();
		driver.session().run("CREATE (:Person{name: 'Somebody', yearBorn: 1982})").consume();

		try (var session = driver.session()) {
			List<Person> people = session.run("MATCH (p:Person) return p")
					.list(mapper.createConverterFor(Person.class));

			logOutput(people);
		}
	}

	public static void mapListOfNodes(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();
		driver.session().run("CREATE (:Person{name: 'Somebody', yearBorn: 1982})").consume();

		try (var session = driver.session()) {
			Record personCollectionRecord = session.run("MATCH (p:Person) return collect(p)").single();
			Iterable<Person> people = mapper.createCollectionConverterFor(Person.class).apply(personCollectionRecord);

			logOutput(people);
		}
	}

	public static void mapMultipleListsOfNodes(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();
		driver.session().run("CREATE (:Person{name: 'Somebody', yearBorn: 1982})").consume();

		try (var session = driver.session()) {
			List<Iterable<Person>> people = session.run("MATCH (p:Person) return collect(p)")
					.list(mapper.createCollectionConverterFor(Person.class));

			logOutput(people);
		}
	}

	public static void mapFromValueMap(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();

		try (var session = driver.session()) {
			Record singleNode = session.run("MATCH (p:Person) return p{.name, .yearBorn}").single();
			Person person = mapper.createConverterFor(Person.class).apply(singleNode);

			logOutput(person);
		}
	}

	public static void mapFromMultipleValues(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();

		try (var session = driver.session()) {
			Record singleNode = session.run("MATCH (p:Person) return p.name as name, p.yearBorn as yearBorn").single();
			Person person = mapper.createConverterFor(Person.class).apply(singleNode);

			logOutput(person);
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
