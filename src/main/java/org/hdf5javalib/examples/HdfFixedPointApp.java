package org.hdf5javalib.examples;

import lombok.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hdf5javalib.HdfFileReader;
import org.hdf5javalib.dataclass.HdfFixedPoint;
import org.hdf5javalib.dataclass.HdfString;
import org.hdf5javalib.datasource.DataClassDataSource;
import org.hdf5javalib.datasource.DataClassMatrixDataSource;
import org.hdf5javalib.file.HdfDataSet;
import org.hdf5javalib.file.HdfFile;
import org.hdf5javalib.file.dataobject.message.DataspaceMessage;
import org.hdf5javalib.file.dataobject.message.DatatypeMessage;
import org.hdf5javalib.file.dataobject.message.datatype.FixedPointDatatype;
import org.hdf5javalib.file.dataobject.message.datatype.StringDatatype;
import org.hdf5javalib.utils.HdfTypeUtils;
import org.hdf5javalib.utils.HdfWriteUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class HdfFixedPointApp {
    public static void main(String[] args)  {
        new HdfFixedPointApp().run();
    }
    private void run() {
        try {
            HdfFileReader reader = new HdfFileReader();
            String filePath = HdfCompoundApp.class.getResource("/singleint.h5").getFile();
            try(FileInputStream fis = new FileInputStream(filePath)) {
                FileChannel channel = fis.getChannel();
                reader.readFile(channel);
                tryScalarDataSpliterator(channel, reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        tryHdfApiCompound();
        try {
            HdfFileReader reader = new HdfFileReader();
            String filePath = HdfFixedPointApp.class.getResource("/randomints.h5").getFile();
            try(FileInputStream fis = new FileInputStream(filePath)) {
                FileChannel channel = fis.getChannel();
                reader.readFile(channel);
                tryTemperatureSpliterator(channel, reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        tryHdfApiInts();
        try {
            HdfFileReader reader = new HdfFileReader();
            String filePath = HdfFixedPointApp.class.getResource("/weather_data.h5").getFile();
            try(FileInputStream fis = new FileInputStream(filePath)) {
                FileChannel channel = fis.getChannel();
                reader.readFile(channel);
                tryWeatherSpliterator(channel, reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        tryHdfApiInts("randomintseach.h5", this::writeEach);
//        tryHdfApiInts("randomintsall.h5", this::writeAll);
        tryHdfApiMatrixInts("weather_data.h5", this::writeEachMatrix);
    }

    @Data
    public static class Scalar {
        private BigInteger data;
    }

    private void tryScalarDataSpliterator(FileChannel fileChannel, HdfFileReader reader) throws IOException {
        DataClassDataSource<HdfFixedPoint> dataSource = new DataClassDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), HdfFixedPoint.class);
        HdfFixedPoint[] allData = dataSource.readAll();
        System.out.println("Scalar readAll stats = " + Arrays.stream(allData).map(HdfFixedPoint::toBigInteger).collect(Collectors.summarizingInt(BigInteger::intValue)));
        System.out.println("Scalar streaming list = " + dataSource.stream().map(fp->HdfTypeUtils.populateFromFixedPoint(Scalar.class, "data", fp, 0)).toList());
        System.out.println("Scalar parallelStreaming list = " + dataSource.parallelStream().map(fp->HdfTypeUtils.populateFromFixedPoint(Scalar.class, "data", fp, 0)).toList());
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TemperatureData {
        private BigInteger temperature;
    }

    public void tryTemperatureSpliterator(FileChannel fileChannel, HdfFileReader reader) throws IOException {
        DataClassDataSource<HdfFixedPoint> dataSource = new DataClassDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), HdfFixedPoint.class);
        HdfFixedPoint[] allData = dataSource.readAll();
        System.out.println("Vector readAll stats  = " + Arrays.stream(allData).map(HdfFixedPoint::toBigInteger).collect(Collectors.summarizingInt(BigInteger::intValue)));
        System.out.println("Vector streaming stats = " + dataSource.stream()
                .map(fp->HdfTypeUtils.populateFromFixedPoint(TemperatureData.class, "temperature", fp, 0))
                .map(TemperatureData::getTemperature)
                .collect(Collectors.summarizingInt(BigInteger::intValue)));
        System.out.println("Vector parallel streaming stats = " + dataSource.parallelStream()
                .map(fp->HdfTypeUtils.populateFromFixedPoint(TemperatureData.class, "temperature", fp, 0))
                .map(TemperatureData::getTemperature)
                .collect(Collectors.summarizingInt(BigInteger::intValue)));
    }

    private void tryWeatherSpliterator(FileChannel fileChannel, HdfFileReader reader) throws IOException {
        DataClassMatrixDataSource<HdfFixedPoint> dataSource = new DataClassMatrixDataSource<>(reader.getDataObjectHeaderPrefix(), 2, fileChannel, reader.getDataAddress(), HdfFixedPoint.class);
        HdfFixedPoint[][] allData = dataSource.readAll();
        // Print the matrix values
        System.out.println("Matrix readAll() = ");
        for (HdfFixedPoint[] allDatum : allData) {
            for (HdfFixedPoint hdfFixedPoint : allDatum) {
                System.out.print(hdfFixedPoint.toBigDecimal(2) + " ");
            }
            System.out.println(); // New line after each row
        }

        Stream<HdfFixedPoint[]> stream = dataSource.stream();
        // Print all values
        System.out.println("Matrix stream() = ");
        stream.forEach(array -> {
            for (HdfFixedPoint value : array) {
                System.out.print(value.toBigDecimal(2) + " ");
            }
            System.out.println(); // Newline after each array
        });
        Stream<HdfFixedPoint[]> parallelStream = dataSource.parallelStream();

        // Print all values in order
        System.out.println("Matrix parallelStream() = ");
        parallelStream.forEachOrdered(array -> {
            for (HdfFixedPoint value : array) {
                System.out.print(value.toBigDecimal(2) + " ");
            }
            System.out.println();
        });
    }

    private void tryHdfApiMatrixInts(String FILE_NAME, Consumer<MatrixWriterParams> writer) {
        String filePath = "/weatherdata.csv";

        List<String> labels;
        List<List<BigDecimal>> data = new ArrayList<>();
        try (Reader reader = new InputStreamReader(HdfFixedPointApp.class.getResourceAsStream(filePath), StandardCharsets.UTF_8)) {
            // Updated approach to set header and skip the first record
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();

            CSVParser parser = new CSVParser(reader, csvFormat);

            // Get column labels
            labels = new ArrayList<>(parser.getHeaderNames());
            System.out.println("Labels: " + labels);

            // Read rows and convert values to BigDecimal

            for (CSVRecord record : parser) {
                List<BigDecimal> row = new ArrayList<>();
                for (String label : labels) {
                    String value = record.get(label);
                    if (value != null && !value.isEmpty()) {
                        row.add(new BigDecimal(value).setScale(2, RoundingMode.HALF_UP));
                    } else {
                        row.add(null); // Handle missing values
                    }
                }
                data.add(row);
            }

            // Print sample data
            System.out.println("Data:");
            for (List<BigDecimal> row : data) {
                System.out.println(row);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
//            final String FILE_NAME = "randomintseach.h5";
            final StandardOpenOption[] FILE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
            final String DATASET_NAME = "Data";
            final int NUM_RECORDS = 4;
            final int NUM_DATAPOINTS = 17;

            // Create a new HDF5 file
            HdfFile file = new HdfFile(FILE_NAME, FILE_OPTIONS);

            // Create data space
            HdfFixedPoint[] hdfDimensions = {HdfFixedPoint.of(NUM_RECORDS), HdfFixedPoint.of(NUM_DATAPOINTS)};
            DataspaceMessage dataSpaceMessage = new DataspaceMessage(1, 2, 1, hdfDimensions, hdfDimensions, false);
//            hsize_t dim[1] = { NUM_RECORDS };
//            DataSpace space(1, dim);

            FixedPointDatatype fixedPointDatatype = new FixedPointDatatype(
                    FixedPointDatatype.createClassAndVersion(),
                    FixedPointDatatype.createClassBitField( false, false, false, false),
                    (short)4, (short)7, (short)25);

            // Create dataset
//            DataSet dataset = file.createDataSet(DATASET_NAME, compoundType, space);
            HdfDataSet dataset = file.createDataSet(DATASET_NAME, fixedPointDatatype, dataSpaceMessage);

//            writeVersionAttribute(dataset);

            writer.accept(new MatrixWriterParams(NUM_RECORDS, NUM_DATAPOINTS, fixedPointDatatype, dataset, data));

            dataset.close();
            file.close();

            // auto close

            System.out.println("HDF5 file " + FILE_NAME + " created and written successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void tryHdfApiInts(String FILE_NAME, Consumer<WriterParams> writer) {
//        final String FILE_NAME = "randomintseach.h5";
        final StandardOpenOption[] FILE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
        final String DATASET_NAME = "temperature";
        final int NUM_RECORDS = 100;

        try {
            // Create a new HDF5 file
            HdfFile file = new HdfFile(FILE_NAME, FILE_OPTIONS);

            // Create data space
            HdfFixedPoint[] hdfDimensions = {HdfFixedPoint.of(NUM_RECORDS)};
            DataspaceMessage dataSpaceMessage = new DataspaceMessage(1, 1, 1, hdfDimensions, hdfDimensions, false);
//            hsize_t dim[1] = { NUM_RECORDS };
//            DataSpace space(1, dim);

            FixedPointDatatype fixedPointDatatype = new FixedPointDatatype(
                    FixedPointDatatype.createClassAndVersion(),
                    FixedPointDatatype.createClassBitField( false, false, false, true),
                    (short)8, (short)0, (short)64);

            // Create dataset
//            DataSet dataset = file.createDataSet(DATASET_NAME, compoundType, space);
            HdfDataSet dataset = file.createDataSet(DATASET_NAME, fixedPointDatatype, dataSpaceMessage);

            writeVersionAttribute(dataset);

            writer.accept(new WriterParams(NUM_RECORDS, fixedPointDatatype, dataset));

            dataset.close();
            file.close();

            // auto close

            System.out.println("HDF5 file " + FILE_NAME + " created and written successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private void writeVersionAttribute(HdfDataSet dataset) {
        String ATTRIBUTE_NAME = "GIT root revision";
        String ATTRIBUTE_VALUE = "Revision: , URL: ";
        BitSet classBitField = StringDatatype.getStringTypeBitSet(StringDatatype.PaddingType.NULL_TERMINATE, StringDatatype.CharacterSet.ASCII);
        // value
        StringDatatype attributeType = new StringDatatype(StringDatatype.createClassAndVersion(), classBitField, (short)ATTRIBUTE_VALUE.length());
        // data type, String, DATASET_NAME.length
        DatatypeMessage dt = new DatatypeMessage(attributeType);
        // scalar, 1 string
        DataspaceMessage ds = new DataspaceMessage(1, 0, 0, null, null, false);
        HdfString hdfString = new HdfString(ATTRIBUTE_VALUE.getBytes(), classBitField);
        dataset.createAttribute(ATTRIBUTE_NAME, dt, ds, hdfString);
    }

    @SneakyThrows
    private void writeEachMatrix(MatrixWriterParams writerParams) {
        AtomicInteger countHolder = new AtomicInteger(0);
//            DataClassDataSource<HdfFixedPoint> dataSource = new DataClassDataSource<>(dataset.getDataObjectHeaderPrefix(), 0, null, 0, HdfFixedPoint.class);
        ByteBuffer byteBuffer = ByteBuffer.allocate(writerParams.fixedPointDatatype.getSize() * writerParams.NUM_DATAPOINTS).order(writerParams.fixedPointDatatype.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        // Write to dataset
        writerParams.dataset.write(() -> {
            int count = countHolder.getAndIncrement();
            if (count >= writerParams.NUM_RECORDS) return ByteBuffer.allocate(0);
            byteBuffer.clear();
            BigDecimal twoShifted = new BigDecimal(BigInteger.ONE.shiftLeft(writerParams.fixedPointDatatype.getBitOffset()));
            for(int i=0; i < writerParams.NUM_DATAPOINTS; ++i ) {
                BigDecimal rawValue = writerParams.values.get(count).get(i).multiply(twoShifted);
                BigInteger rawValueShifted = rawValue.toBigInteger();
                new HdfFixedPoint(rawValueShifted,
                        writerParams.fixedPointDatatype.getSize(),
                        writerParams.fixedPointDatatype.isBigEndian(),
                        writerParams.fixedPointDatatype.isLoPad(),
                        writerParams.fixedPointDatatype.isHiPad(),
                        writerParams.fixedPointDatatype.isSigned(),
                        writerParams.fixedPointDatatype.getBitOffset(),
                        writerParams.fixedPointDatatype.getBitPrecision())
                        .writeValueToByteBuffer(byteBuffer);
            }
            byteBuffer.flip();
            return byteBuffer;
        });
    }

    @SneakyThrows
    private void writeEach(WriterParams writerParams) {
        AtomicInteger countHolder = new AtomicInteger(0);
//            DataClassDataSource<HdfFixedPoint> dataSource = new DataClassDataSource<>(dataset.getDataObjectHeaderPrefix(), 0, null, 0, HdfFixedPoint.class);
        ByteBuffer byteBuffer = ByteBuffer.allocate(writerParams.fixedPointDatatype.getSize());
        // Write to dataset
        writerParams.dataset.write(() -> {
            int count = countHolder.getAndIncrement();
            if (count >= writerParams.NUM_RECORDS) return ByteBuffer.allocate(0);
            byteBuffer.clear();
            HdfWriteUtils.writeBigIntegerAsHdfFixedPoint(BigInteger.valueOf((long) (Math.random() *40.0 + 10.0)), writerParams.fixedPointDatatype, byteBuffer);
            byteBuffer.flip();
            return byteBuffer;
        });
    }

    @SneakyThrows
    private void writeAll(WriterParams writerParams) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(writerParams.fixedPointDatatype.getSize() * writerParams.NUM_RECORDS);
        for(int i=0; i<writerParams.NUM_RECORDS; i++) {
            HdfWriteUtils.writeBigIntegerAsHdfFixedPoint(BigInteger.valueOf((long) (Math.random() *40.0 + 10.0)), writerParams.fixedPointDatatype, byteBuffer);
        }
        byteBuffer.flip();
        // Write to dataset
        writerParams.dataset.write(byteBuffer);
    }

    @AllArgsConstructor
    static class WriterParams {
        int NUM_RECORDS;
        FixedPointDatatype fixedPointDatatype;
        HdfDataSet dataset;
    }

    @AllArgsConstructor
    static class MatrixWriterParams {
        int NUM_RECORDS;
        int NUM_DATAPOINTS;
        FixedPointDatatype fixedPointDatatype;
        HdfDataSet dataset;
        List<List<BigDecimal>> values;
    }


}
