package com.meistermeier.neo4j.toolbelt.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ConverterTest {

	Converter converter = Converter.INSTANCE.apply(TypeSystem.getDefault());

	private static Stream<Arguments> convertSimpleTypes() {
		LocalDate localDate = LocalDate.now();
		LocalDateTime localDateTime = LocalDateTime.now();
		LocalTime localTime = LocalTime.now();
		return Stream.of(
				Arguments.of(Values.value(1L), 1L),
				Arguments.of(Values.value("Test"), "Test"),
				Arguments.of(Values.value(true), true),
				Arguments.of(Values.value(2), 2),
				Arguments.of(Values.value(3f), 3f),
				Arguments.of(Values.value(4d), 4d),
				Arguments.of(Values.value(localDate), localDate),
				Arguments.of(Values.value(localDateTime), localDateTime),
				Arguments.of(Values.value(localTime), localTime)
		);
	}

	/**
	 * Covers test cases in the form of
	 * RETURN 1
	 * RETURN 'Test'
	 * etc.
	 */
	@ParameterizedTest
	@MethodSource
	void convertSimpleTypes(Value sourceValue, Object expected) {
		Object result = converter.convertOne(sourceValue, expected.getClass());
		assertThat(result).isEqualTo(expected);
	}

	private static Stream<Arguments> convertListTypes() {
		LocalDate localDate1 = LocalDate.now();
		LocalDate localDate2 = localDate1.plus(1, ChronoUnit.DAYS);
		LocalDateTime localDateTime1 = LocalDateTime.now();
		LocalDateTime localDateTime2 = localDateTime1.plus(1, ChronoUnit.DAYS);
		LocalTime localTime1 = LocalTime.now();
		LocalTime localTime2 = localTime1.plus(1, ChronoUnit.HOURS);
		return Stream.of(
				Arguments.of(Values.value(1L, 2L), List.of(1L, 2L), Long[].class),
				Arguments.of(Values.value("Test1", "Test2"), List.of("Test1", "Test2"), String[].class),
				Arguments.of(Values.value(true, false), List.of(true, false), Boolean[].class),
				Arguments.of(Values.value(2, 3), List.of(2, 3), Integer[].class),
				Arguments.of(Values.value(3f, 4f), List.of(3f, 4f), Float[].class),
				Arguments.of(Values.value(4d, 5d), List.of(4d, 5d), Double[].class),
				Arguments.of(Values.value(List.of(localDate1, localDate2).toArray()), List.of(localDate1, localDate2), LocalDate[].class),
				Arguments.of(Values.value(List.of(localDateTime1, localDateTime2).toArray()), List.of(localDateTime1, localDateTime2), LocalDateTime[].class),
				Arguments.of(Values.value(List.of(localTime1, localTime2).toArray()), List.of(localTime1, localTime2), LocalTime[].class)
		);
	}

	/**
	 * Covers test cases in the form of
	 * RETURN [1, 2]
	 * RETURN ['Test1', 'Test2']
	 * etc.
	 */
	@ParameterizedTest
	@MethodSource
	void convertListTypes(Value sourceValue, Object expected, Class<?> expectedClass) {
		Object result = converter.convertOne(sourceValue, expectedClass);
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void convertOneFieldToRecord() {
		MapAccessor record = Values.value(Map.of("a", "a"));

		ConversionTargetRecord conversionTarget = converter.convertOne(record, ConversionTargetRecord.class);
		assertThat(conversionTarget.a).isEqualTo("a");
		assertThat(conversionTarget.b).isNull();
	}

	@Test
	void convertMultipleFieldsToRecord() {
		MapAccessor record = Values.value(Map.of("a", "a", "b", "b"));

		ConversionTargetRecord conversionTarget = converter.convertOne(record, ConversionTargetRecord.class);
		assertThat(conversionTarget.a).isEqualTo("a");
		assertThat(conversionTarget.b).isEqualTo("b");
	}

	@Test
	void convertCollectionFieldToRecord() {
		MapAccessor record = Values.value(Map.of("a", "a", "b", "b", "c", Values.value("a", "b", "c")));

		ConversionTargetRecord conversionTarget = converter.convertOne(record, ConversionTargetRecord.class);
		assertThat(conversionTarget.a).isEqualTo("a");
		assertThat(conversionTarget.b).isEqualTo("b");
		assertThat(conversionTarget.c)
				.hasSize(3)
				.containsExactly("a", "b", "c");
	}

	@Test
	void convertUnorderedFieldsToRecord() {
		MapAccessor record = Values.value(Map.of("c", Values.value("a", "b", "c"), "a", "a", "b", "b"));

		ConversionTargetRecord conversionTarget = converter.convertOne(record, ConversionTargetRecord.class);
		assertThat(conversionTarget.a).isEqualTo("a");
		assertThat(conversionTarget.b).isEqualTo("b");
		assertThat(conversionTarget.c)
				.hasSize(3)
				.containsExactly("a", "b", "c");
	}

	@Test
	void convertOneFieldToClass() {
		MapAccessor record = Values.value(Map.of("a", "a"));

		ConversionTargetClass conversionTarget = converter.convertOne(record, ConversionTargetClass.class);
		assertThat(conversionTarget.a).isEqualTo("a");
		assertThat(conversionTarget.b).isNull();
	}

	@Test
	void convertMultipleFieldsToClass() {
		MapAccessor record = Values.value(Map.of("a", "a", "b", "b"));

		ConversionTargetClass conversionTarget = converter.convertOne(record, ConversionTargetClass.class);
		assertThat(conversionTarget.a).isEqualTo("a");
		assertThat(conversionTarget.b).isEqualTo("b");
	}

	@Test
	void convertCollectionFieldToClass() {
		MapAccessor record = Values.value(Map.of("a", "a", "b", "b", "c", Values.value("a", "b", "c")));

		ConversionTargetClass conversionTarget = converter.convertOne(record, ConversionTargetClass.class);
		assertThat(conversionTarget.a).isEqualTo("a");
		assertThat(conversionTarget.b).isEqualTo("b");
		assertThat(conversionTarget.c)
				.hasSize(3)
				.containsExactly("a", "b", "c");
	}

	@Test
	void convertUnorderedFieldsToClass() {
		MapAccessor record = Values.value(Map.of("c", Values.value("a", "b", "c"), "a", "a", "b", "b"));

		ConversionTargetClass conversionTarget = converter.convertOne(record, ConversionTargetClass.class);
		assertThat(conversionTarget.a).isEqualTo("a");
		assertThat(conversionTarget.b).isEqualTo("b");
		assertThat(conversionTarget.c)
				.hasSize(3)
				.containsExactly("a", "b", "c");
	}

	record ConversionTargetRecord(String a, String b, List<String> c) {
	}

	static class ConversionTargetClass {
		public final String a;
		public final String b;
		public final List<String> c;

		ConversionTargetClass(String a, String b, List<String> c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}

}
