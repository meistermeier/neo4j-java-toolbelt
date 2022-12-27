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
		renderClassAsParameters(driver);

		driver.close();
	}

	static void renderRecordAsParameters(Driver driver) {
		try (var session = driver.session()) {
			ParameterRecord parameterRecord = new ParameterRecord("test", 4711);
			Value parameters = Renderer.INSTANCE.toParameters(parameterRecord);
			Record result = session.run("RETURN $a, $b", parameters).single();
			System.out.println(result); // Record<{$a: "test", $b: 4711}>
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

	record ParameterRecord(String a, int b) {}

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
