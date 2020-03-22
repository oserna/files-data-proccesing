package com.example.demo;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DemoApplicationTests {

	private final DateTimeFormatter dataTimeFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneOffset.UTC);

	private final DateTimeFormatter indexTimeFormatter = DateTimeFormatter
			.ofPattern("dd/MM/yyyy HH:mm:ss")
			.withZone(ZoneOffset.UTC);

	@Test
	public void instant() {

		final String eventTime = "2020-03-04 10:24:52";

		final Instant instant0 = Instant.from(dataTimeFormatter.parse(eventTime));

		final LocalDateTime datetime = LocalDateTime.ofInstant(instant0, ZoneOffset.UTC);

		final String formatted = dataTimeFormatter.format(datetime);

		assertEquals(eventTime, formatted);

	}

	@Test
	public void testIndexTimeFormatter() {

		final String eventTime = "04/03/2020 13:27:55";

		final Instant instant0 = Instant.from(indexTimeFormatter.parse(eventTime));

		final LocalDateTime datetime = LocalDateTime.ofInstant(instant0, ZoneOffset.UTC);

		final String formatted = indexTimeFormatter.format(datetime);

		assertEquals(eventTime, formatted);

	}


	@Test
	public void testEventTimeInRange() {

		final String eventTime = "2020-03-04 10:24:55";
		final Instant instantEventTime = Instant.from(dataTimeFormatter.parse(eventTime));
		TimeRange range = new TimeRange(instantEventTime, 10, 2);

		final String[] ets = {
				"2020-03-04 10:24:45",
				"2020-03-04 10:24:47",
				"2020-03-04 10:24:50",
				"2020-03-04 10:24:53",
				"2020-03-04 10:24:55",
				"2020-03-04 10:24:56",
				};

		final List<Instant> withinRange = Stream.of(ets)
				.map(et -> Instant.from(dataTimeFormatter.parse(et)))
				.filter(i -> range.contains(i))
				.collect(Collectors.toList());

		assertEquals(ets.length, withinRange.size());

	}

	@Test
	public void testFileReader() {
		final List<CSVReader.Data> dataList = CSVReader.readData();
		assertEquals(dataList.size(), 2371);
	}

	@Test
	public void testIndexesReader() {
		final List<CSVReader.Index> dataList = CSVReader.readIndexes();
		assertEquals(dataList.size(), 46);
	}
}
