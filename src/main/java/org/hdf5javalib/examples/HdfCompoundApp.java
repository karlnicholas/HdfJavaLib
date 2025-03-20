package org.hdf5javalib.examples;

import lombok.Data;
import org.hdf5javalib.HdfFileReader;
import org.hdf5javalib.dataclass.HdfCompound;
import org.hdf5javalib.dataclass.HdfFixedPoint;
import org.hdf5javalib.dataclass.HdfString;
import org.hdf5javalib.datasource.TypedDataSource;
import org.hdf5javalib.file.HdfDataSet;
import org.hdf5javalib.file.HdfFile;
import org.hdf5javalib.file.dataobject.message.DataspaceMessage;
import org.hdf5javalib.file.dataobject.message.DatatypeMessage;
import org.hdf5javalib.file.dataobject.message.datatype.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public class HdfCompoundApp {
    public static void main(String[] args) {
        new HdfCompoundApp().run();
    }

    private void run() {
        try {
            HdfFileReader reader = new HdfFileReader();
            String filePath = HdfCompoundApp.class.getResource("/compound_example_gpt.h5").getFile();
            try (FileInputStream fis = new FileInputStream(filePath)) {
                FileChannel channel = fis.getChannel();
                reader.readFile(channel);
                tryCompoundTestSpliterator(channel, reader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        try {
//            HdfFileReader reader = new HdfFileReader();
//            String filePath = HdfCompoundApp.class.getResource("/env_monitoring_labels.h5").getFile();
//            try (FileInputStream fis = new FileInputStream(filePath)) {
//                FileChannel channel = fis.getChannel();
//                reader.readFile(channel);
//                tryCompoundSpliterator(channel, reader);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            HdfFileReader reader = new HdfFileReader();
//            String filePath = HdfCompoundApp.class.getResource("/env_monitoring.h5").getFile();
//            try (FileInputStream fis = new FileInputStream(filePath)) {
//                FileChannel channel = fis.getChannel();
//                reader.readFile(channel);
//                tryCustomSpliterator(channel, reader);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        tryHdfApiCompound();
    }

    private void writeVersionAttribute(HdfDataSet dataset) {
        String ATTRIBUTE_NAME = "GIT root revision";
        String ATTRIBUTE_VALUE = "Revision: , URL: ";
        BitSet classBitField = StringDatatype.createClassBitField(StringDatatype.PaddingType.NULL_TERMINATE, StringDatatype.CharacterSet.ASCII);
        // value
        StringDatatype attributeType = new StringDatatype(StringDatatype.createClassAndVersion(), classBitField, (short) ATTRIBUTE_VALUE.length());
        // data type, String, DATASET_NAME.length
        short dataTypeMessageSize = 8;
        dataTypeMessageSize += attributeType.getSizeMessageData();
        // to 8 byte boundary
        dataTypeMessageSize += ((dataTypeMessageSize + 7) & ~7);
        DatatypeMessage dt = new DatatypeMessage(attributeType, (byte)1, dataTypeMessageSize);
        // scalar, 1 string
        short dataspaceMessageSize = 8;
        DataspaceMessage ds = new DataspaceMessage(1, 0, 0, null, null, false, (byte)0, dataspaceMessageSize);
        HdfString hdfString = new HdfString(ATTRIBUTE_VALUE.getBytes(), attributeType);
        dataset.createAttribute(ATTRIBUTE_NAME, dt, ds, hdfString);
    }

    public void tryHdfApiCompound() {
        final String FILE_NAME = "compound_example.h5";
        final StandardOpenOption[] FILE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
        final String DATASET_NAME = "CompoundData";
        final int NUM_RECORDS = 1000;

        try {
            // Create a new HDF5 file
            HdfFile file = new HdfFile(FILE_NAME, FILE_OPTIONS);

            List<CompoundMemberDatatype> compoundData = List.of(
                    new CompoundMemberDatatype("recordId", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, false),
                                    (short) 8, (short) 0, (short) 64)),
                    new CompoundMemberDatatype("fixedStr", 8, 0, 0, new int[4],
                            new StringDatatype(
                                    StringDatatype.createClassAndVersion(),
                                    StringDatatype.createClassBitField(StringDatatype.PaddingType.NULL_TERMINATE, StringDatatype.CharacterSet.ASCII),
                                    (short) 10)),
                    new CompoundMemberDatatype("varStr", 8, 0, 0, new int[4],
                            new VariableLengthDatatype(
                                    VariableLengthDatatype.createClassAndVersion(),
                                    VariableLengthDatatype.createClassBitField(VariableLengthDatatype.PaddingType.NULL_TERMINATE, VariableLengthDatatype.CharacterSet.ASCII),
                                    (short) 10,
                                    0)),
                    new CompoundMemberDatatype("floatVal", 15, 0, 0, new int[4],
                            new FloatingPointDatatype(
                                    FloatingPointDatatype.createClassAndVersion(),
                                    FloatingPointDatatype.createClassBitField(false),
                                    4, (short) 0, (short) 32, (byte) 23, (byte) 8, (byte) 0, (byte) 23, 127)),
                    new CompoundMemberDatatype("floatVal", 15, 0, 0, new int[4],
                            new FloatingPointDatatype(
                                    FloatingPointDatatype.createClassAndVersion(),
                                    FloatingPointDatatype.createClassBitField(false),
                                    8, (short) 0, (short) 64, (byte) 52, (byte) 11, (byte) 0, (byte) 52, 1023)),
                    new CompoundMemberDatatype("int8_Val", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, true),
                                    (short) 1, (short) 0, (short) 8)),
                    new CompoundMemberDatatype("uint8_Val", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, false),
                                    (short) 1, (short) 0, (short) 8)),
                    new CompoundMemberDatatype("int16_Val", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, true),
                                    (short) 2, (short) 0, (short) 16)),
                    new CompoundMemberDatatype("uint16_Val", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, false),
                                    (short) 2, (short) 0, (short) 16)),
                    new CompoundMemberDatatype("int32_Val", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, true),
                                    (short) 4, (short) 0, (short) 32)),
                    new CompoundMemberDatatype("uint32_Val", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, false),
                                    (short) 4, (short) 0, (short) 32)),
                    new CompoundMemberDatatype("int64_Val", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, true),
                                    (short) 8, (short) 0, (short) 64)),
                    new CompoundMemberDatatype("uint64_Val", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, false),
                                    (short) 8, (short) 0, (short) 64)),
                    new CompoundMemberDatatype("bitfieldVal", 0, 0, 0, new int[4],
                            new FixedPointDatatype(
                                    FixedPointDatatype.createClassAndVersion(),
                                    FixedPointDatatype.createClassBitField(false, false, false, false),
                                    (short) 8, (short) 7, (short) 57))
            );
            short compoundSize = (short) compoundData.stream().mapToInt(c -> c.getType().getSize()).sum();

            // Define Compound DataType correctly
            CompoundDatatype compoundType = new CompoundDatatype(CompoundDatatype.createClassAndVersion(), CompoundDatatype.createClassBitField((short) compoundData.size()), compoundSize, compoundData);
//            DatatypeMessage dataTypeMessage = new DatatypeMessage((byte) 1, (byte) 6, BitSet.valueOf(new byte[]{0b10001}), 56, compoundType);

            // Create data space
            HdfFixedPoint[] hdfDimensions = {HdfFixedPoint.of(NUM_RECORDS)};
            short dataspaceMessageSize = 8;
            if ( hdfDimensions != null ) {
                for (HdfFixedPoint dimension : hdfDimensions) {
                    dataspaceMessageSize += dimension.getDatatype().getSize();
                }
            }
            if ( hdfDimensions != null ) {
                for (HdfFixedPoint maxDimension : hdfDimensions) {
                    dataspaceMessageSize += maxDimension.getDatatype().getSize();
                }
            }
            DataspaceMessage dataSpaceMessage = new DataspaceMessage(1, 1, 1, hdfDimensions, hdfDimensions, false, (byte)0, dataspaceMessageSize);
//            hsize_t dim[1] = { NUM_RECORDS };
//            DataSpace space(1, dim);

            // Create dataset
//            DataSet dataset = file.createDataSet(DATASET_NAME, compoundType, space);
            HdfDataSet dataset = file.createDataSet(DATASET_NAME, compoundType, dataSpaceMessage);


            // ADD ATTRIBUTE: "GIT root revision"
//            writeVersionAttribute(dataset);

//            AtomicInteger countHolder = new AtomicInteger(0);
//            TypedDataSource<ShipperData> volumeDataHdfDataSource = new TypedDataSource<>(dataset.getDataObjectHeaderPrefix(), 0, file., 0, ShipperData.class);
//            ByteBuffer buffer = ByteBuffer.allocate(compoundType.getSize()).order(ByteOrder.LITTLE_ENDIAN);
            // Write to dataset
//            dataset.write(() -> {
//                int count = countHolder.getAndIncrement();
//                if (count >= NUM_RECORDS) return  ByteBuffer.allocate(0);
//                ShipperData instance = ShipperData.builder()
//                        .shipmentId(BigInteger.valueOf(count + 1000))
//                        .origCountry("US")
//                        .origSlic("12345")
//                        .origSort(BigInteger.valueOf(4))
//                        .destCountry("CA")
//                        .destSlic("67890")
//                        .destIbi(BigInteger.valueOf(0))
//                        .destPostalCode("A1B2C3")
//                        .shipper("FedEx")
//                        .service(BigInteger.valueOf(0))
//                        .packageType(BigInteger.valueOf(3))
//                        .accessorials(BigInteger.valueOf(0))
//                        .pieces(BigInteger.valueOf(2))
//                        .weight(BigInteger.valueOf(50))
//                        .cube(BigInteger.valueOf(1200))
//                        .committedTnt(BigInteger.valueOf(255))
//                        .committedDate(BigInteger.valueOf(3))
//                        .build();
//                buffer.clear();
//                HdfWriteUtils.writeCompoundTypeToBuffer(instance, compoundType, buffer, ShipperData.class);
//                buffer.position(0);
//                return buffer;
//            });

            dataset.close();
            file.close();

            // auto close

            System.out.println("HDF5 file created and written successfully!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    public static class CompoundExample {
        private Long recordId;
        private String fixedStr;
        private String varStr;
        private Float floatVal;
        private Double doubleVal;
        private Byte int8_Val;
        private Short int16_Val;
        private Integer int32_Val;
        private Long int64_Val;
        private Short uint8_Val;
        private Integer uint16_Val;
        private Long uint32_Val;
        private BigInteger uint64_Val;
        private BigDecimal bitfieldVal;
    }

    @Data
    public static class MonitoringData {
        private String siteName;
        private Float airQualityIndex;
        private Double temperature;
        private Integer sampleCount;
    }


    public void tryCompoundTestSpliterator(FileChannel fileChannel, HdfFileReader reader) throws IOException {
        System.out.println("Count = " + new TypedDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), HdfCompound.class).stream().count());

        System.out.println("Ten Rows:");
        new TypedDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), HdfCompound.class)
                .stream()
                .limit(10)
                .forEach(c -> System.out.println("Row: " + c.getMembers()));

        System.out.println("Ten BigDecimals = " + new TypedDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), HdfCompound.class).stream()
                        .filter(c->c.getMembers().get(0).getInstance(Long.class).longValue() < 1010 )
                .map(c->c.getMembers().get(13).getInstance(BigDecimal.class)).toList());

        System.out.println("Ten Rows:");
        new TypedDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), CompoundExample.class)
                .stream()
                .filter(c -> c.getRecordId() < 1010)
                .forEach(c -> System.out.println("Row: " + c));
    }

    public void tryCompoundSpliterator(FileChannel fileChannel, HdfFileReader reader) throws IOException {
        TypedDataSource<MonitoringData> dataSource = new TypedDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), MonitoringData.class);
        MonitoringData[] allData = dataSource.readAll();
        System.out.println("*** readAll: \r\n" + Arrays.asList(allData).stream().map(Object::toString).collect(Collectors.joining("\n")));
        System.out.println("*** stream: \r\n" + dataSource.stream().map(Object::toString).collect(Collectors.joining("\n")));
        System.out.println("\"*** parallelStream: \r\n" + dataSource.parallelStream().map(Object::toString).collect(Collectors.joining("\n")));

        new TypedDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), HdfCompound.class).stream().forEach(System.out::println);
        new TypedDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), String.class).stream().forEach(System.out::println);
    }


    private void tryCustomSpliterator(FileChannel fileChannel, HdfFileReader reader) {
        CompoundDatatype.addConverter(MonitoringData.class, (bytes, datatype) -> {
            MonitoringData monitoringData = new MonitoringData();
            datatype.getMembers().forEach(member -> {
                byte[] memberBytes = Arrays.copyOfRange(bytes, member.getOffset(), member.getOffset() + member.getSize());
                switch (member.getName()) {
                    case "Site Name" -> monitoringData.setSiteName(member.getInstance(String.class, memberBytes));
                    case "Air Quality Index" -> monitoringData.setAirQualityIndex(member.getInstance(Float.class, memberBytes));
                    case "Temperature" -> monitoringData.setTemperature(member.getInstance(Double.class, memberBytes));
                    case "Sample Count" -> monitoringData.setSampleCount(member.getInstance(Integer.class, memberBytes));
                    default -> throw new RuntimeException("Error");
                }
            });
            return monitoringData;
        });
        new TypedDataSource<>(reader.getDataObjectHeaderPrefix(), 0, fileChannel, reader.getDataAddress(), MonitoringData.class).stream().forEach(System.out::println);
    }

}
