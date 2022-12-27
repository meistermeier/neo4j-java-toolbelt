package com.meistermeier.neo4j.toolbelt.examples.mapper;

import com.meistermeier.neo4j.toolbelt.mapper.Mapper;
import com.meistermeier.neo4j.toolbelt.examples.shared.Environment;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.function.Function;

public class RecordReadingExample {

	private static final StackWalker walker = StackWalker.getInstance();

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
			// tag::mapping-function[]
			Function<Record, Person> personConverter = Mapper.INSTANCE.createMapperFor(Person.class);
			// end::mapping-function[]

			// tag::mapping-function-apply[]
			Record singleNode = session.run("MATCH (p:Person) return p").single();
			Person person = personConverter.apply(singleNode);
			// end::mapping-function-apply[]

			// tag::mapping-function-list[]
			List<Person> people = session.run("MATCH (p:Person) return p")
					.list(personConverter);
			// end::mapping-function-list[]

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
					.list(Mapper.INSTANCE.createMapperFor(Person.class));

			logOutput(people);
		}
	}

	public static void mapListOfNodes(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();
		driver.session().run("CREATE (:Person{name: 'Somebody', yearBorn: 1982})").consume();

		try (var session = driver.session()) {
			Record personCollectionRecord = session.run("MATCH (p:Person) return collect(p)").single();
			Iterable<Person> people = Mapper.INSTANCE.createCollectionMapperFor(Person.class).apply(personCollectionRecord);

			logOutput(people);
		}
	}

	public static void mapMultipleListsOfNodes(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();
		driver.session().run("CREATE (:Person{name: 'Somebody', yearBorn: 1982})").consume();

		try (var session = driver.session()) {
			List<Iterable<Person>> people = session.run("MATCH (p:Person) return collect(p)")
					.list(Mapper.INSTANCE.createCollectionMapperFor(Person.class));

			logOutput(people);
		}
	}

	public static void mapFromValueMap(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();

		try (var session = driver.session()) {
			Record singleNode = session.run("MATCH (p:Person) return p{.name, .yearBorn}").single();
			Person person = Mapper.INSTANCE.createMapperFor(Person.class).apply(singleNode);

			logOutput(person);
		}
	}

	public static void mapFromMultipleValues(Driver driver) {
		driver.session().run("MATCH (n) detach delete n").consume();
		driver.session().run("CREATE (:Person{name: 'Gerrit', yearBorn: 1983})").consume();

		try (var session = driver.session()) {
			Record singleNode = session.run("MATCH (p:Person) return p.name as name, p.yearBorn as yearBorn").single();
			Person person = Mapper.INSTANCE.createMapperFor(Person.class).apply(singleNode);

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
