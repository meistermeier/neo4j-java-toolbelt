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
package com.meistermeier.neo4j.toolbelt.examples.parameters;

import com.meistermeier.neo4j.toolbelt.examples.shared.Environment;
import com.meistermeier.neo4j.toolbelt.renderer.Renderer;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

import java.util.List;

/**
 * @author Gerrit Meier
 */
public class ParameterRenderingExample {

	public static void main(String[] args) {
		Driver driver = Environment.getDriver();
		renderRecordAsParameters(driver);
		renderCollectionOfRecordsAsParameters(driver);
		renderNamedCollectionOfRecordsAsParameters(driver);
		renderClassAsParameters(driver);

		driver.close();
	}

	static void renderRecordAsParameters(Driver driver) {
		try (var session = driver.session()) {
			// tag::parameter-record-use[]
			ParameterRecord parameterRecord = new ParameterRecord("test", 4711);
			Value parameters = Renderer.INSTANCE.toParameters(parameterRecord);
			Record result = session.run("RETURN $a, $b", parameters).single();
			System.out.println(result); // Record<{$a: "test", $b: 4711}>
			// end::parameter-record-use[]
		}
	}

	static void renderCollectionOfRecordsAsParameters(Driver driver) {
		try (var session = driver.session()) {
			// tag::parameter-record-collection-use[]
			ParameterRecord parameterRecord1 = new ParameterRecord("test1", 4711);
			ParameterRecord parameterRecord2 = new ParameterRecord("test2", 42);
			Value parameters = Renderer.INSTANCE.toParameters(List.of(parameterRecord1, parameterRecord2));
			session.run("UNWIND $rows as row RETURN row.a, row.b", parameters).forEachRemaining(
					System.out::println
			);
			// Record<{row.a: "test1", row.b: 4711}>
			// Record<{row.a: "test2", row.b: 42}>
			// end::parameter-record-collection-use[]
		}
	}

	static void renderNamedCollectionOfRecordsAsParameters(Driver driver) {
		try (var session = driver.session()) {
			// tag::parameter-record-named-collection-use[]
			ParameterRecord parameterRecord1 = new ParameterRecord("test1", 4711);
			ParameterRecord parameterRecord2 = new ParameterRecord("test2", 42);
			Value parameters = Renderer.INSTANCE.toParameters(List.of(parameterRecord1, parameterRecord2), "things");
			session.run("UNWIND $things as thing RETURN thing.a, thing.b", parameters).forEachRemaining(
					System.out::println
			);
			// Record<{thing.a: "test1", thing.b: 4711}>
			// Record<{thing.a: "test2", thing.b: 42}>
			// end::parameter-record-named-collection-use[]
		}
	}

	static void renderClassAsParameters(Driver driver) {
		try (var session = driver.session()) {
			ParameterClass parameterClass = new ParameterClass("ClassTest", List.of("a", "b"));
			Value parameters = Renderer.INSTANCE.toParameters(parameterClass);
			Record result = session.run("RETURN $c, $d", parameters).single();
			System.out.println(result); // Record<{$c: "ClassTest", $d: ["a", "b"]}>
		}
	}
	// tag::parameter-record[]
	record ParameterRecord(String a, int b) {}
	// end::parameter-record[]

	static class ParameterClass {
		// access either via public field
		public final String c;
		private final List<String> d;

		ParameterClass(String c, List<String> d) {
			this.c = c;
			this.d = d;
		}

		// ...or access via public getter
		public List<String> getD() {
			return d;
		}
	}
}
