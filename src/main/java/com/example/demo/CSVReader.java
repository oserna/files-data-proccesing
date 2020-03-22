package com.example.demo;

import org.apache.commons.io.input.BOMInputStream;

import java.io.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVReader {

    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    public static final String DATA_FILE_NAME = "/data.csv";
    public static final String INDEX_DATA_FILE_NAME = "/data_from_ipad.csv";
    public static final String REPORT_FILE_NAME = "report.csv";

    public static final DateTimeFormatter dataTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter indexTimeFormatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public static final int range = 60;

    public static void main(String[] args) throws IOException {

        final List<Data> dataList = readData();

        final List<Index> indexes = readIndexes();

        final List<String> lines = Stream.of(processIndexes.apply(indexes, dataList))
                .flatMap((Function<List<Tuple<Index, List<Data>>>, Stream<Tuple<Index, List<Data>>>>) Collection::stream)
                .map(CSVReader::convertToCSV)
                .flatMap((Function<List<String>, Stream<String>>) Collection::stream)
                .collect(Collectors.toList());

        writeReport(lines);

    }


    static BiFunction<Index, List<Data>,List<Data>> eventsInIndexRange = (index, dataLines) -> dataLines.stream()
            .filter(data -> new TimeRange(index.instant(), 10, 2).contains(data.instant))
            .collect(Collectors.toList());


    static BiFunction<List<Index>, List<Data>, List<Tuple<Index,List<Data>>>> processIndexes = (indexes, dataLines) -> {
        final List<Tuple<Index, List<Data>>> collect = indexes.stream()
                .map(index -> {
                    return new Tuple<Index, List<Data>>(index, eventsInIndexRange.apply(index, dataLines));
                }).collect(Collectors.toList());
        return collect;
    };

    public static List<String> convertToCSV(Tuple<Index, List<Data>> tuple) {

        return tuple.getV()
                .stream()
                .map(new Function<Data, String>() {
                    @Override
                    public String apply(Data data) {
                        return String.join("|", Arrays.asList(String.valueOf(tuple.getT().id),
                                indexTimeFormatter.format(LocalDateTime.ofInstant(tuple.getT().instant, ZoneOffset.UTC)),
                                dataTimeFormatter.format(LocalDateTime.ofInstant(data.instant, ZoneOffset.UTC)),
                                data.data()));
                    }
                }).collect(Collectors.toList());
    }

    static void writeReport(List<String> lines) throws IOException {
        File csvOutputFile = new File(REPORT_FILE_NAME);
        try (FileWriter pw = new FileWriter(csvOutputFile)) {
            lines.stream()
                    .forEach(l -> {
                        try {
                            pw.append(l);
                            pw.append(NEW_LINE_SEPARATOR);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            pw.flush();
        }
    }

    static class Tuple<T,V>{
        private T t;
        private V v;

        public Tuple(T t, V v) {
            this.t = t;
            this.v = v;
        }

        public T getT() {
            return t;
        }

        public V getV() {
            return v;
        }
    }

    static List<Index> readIndexes(){
        return readFile(INDEX_DATA_FILE_NAME, line -> {
            String[] str = line.split(COMMA_DELIMITER);
            return new Index.IndexBuilder()
                    .withId(str[0])
                    .withEventTime(str[1])
                    .build();
        });
    }

    static List<Data> readData(){
        return readBOMFile(DATA_FILE_NAME, line -> {
            String[] str = line.split(COMMA_DELIMITER);
            return new Data.DataBuilder()
                    .eventTime(str[0].trim())
                    .data(str[1].trim())
                    .build();
        });
    }

    static <T> List<T> readBOMFile(String fileName, LineParser<T> parser){
        final List<T> lines = new ArrayList<>();

        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(
                                     new BOMInputStream(CSVReader.class.getResourceAsStream(fileName))))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(parser.parse(line));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;

    }

    static <T> List<T> readFile(String fileName, LineParser<T> parser){
        final List<T> lines = new ArrayList<>();

        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(CSVReader.class.getResourceAsStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(parser.parse(line));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;

    }

    interface Builder<T> {
        T build();
    }

    interface LineParser<T> {
        T parse(String line);
    }

    static class Index {
        private Instant instant;
        private Integer id;

        public Index(IndexBuilder builder) {
            this.instant = builder.instant;
            this.id = builder.id;
        }

        public Instant instant() { return instant; }
        public Integer id() { return id; }

        public static class IndexBuilder implements Builder<Index>{

            private Instant instant;
            private Integer id;

            public IndexBuilder withEventTime(String eventTime){

                this.instant = Instant.from(indexTimeFormatter.parse(eventTime.trim()));

                return this;
            }

            public IndexBuilder withId(String id){
                this.id = Integer.valueOf(id.trim());
                return this;
            }

            public Index build(){
                return new Index(this);
            }

        }

    }

    static class Data {

        private Instant instant;
        private String data;

        public Data(DataBuilder buider) {
            this.data = buider.data;
            this.instant = buider.instant;
        }

        public Instant instant() {
            return this.instant;
        }

        public String data() {
            return this.data;
        }

        public static class DataBuilder implements Builder<Data>{

            private Instant instant;
            private String data;

            public DataBuilder eventTime(String time){

                this.instant = Instant.from(dataTimeFormatter.parse(time.trim()));

                return this;
            }

            public DataBuilder data(String data){
                this.data = data.trim();
                return this;
            }

            public Data build(){
                return new Data(this);
            }

        }
    }



}