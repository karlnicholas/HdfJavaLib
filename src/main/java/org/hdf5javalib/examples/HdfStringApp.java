package org.hdf5javalib.examples;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hdf5javalib.HdfFileReader;
import org.hdf5javalib.dataclass.HdfFixedPoint;
import org.hdf5javalib.dataclass.HdfString;
import org.hdf5javalib.datasource.TypedDataSource;
import org.hdf5javalib.file.HdfDataSet;
import org.hdf5javalib.file.HdfFile;
import org.hdf5javalib.file.dataobject.message.DataspaceMessage;
import org.hdf5javalib.file.dataobject.message.datatype.StringDatatype;
import org.hdf5javalib.utils.HdfTestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Hello world!
 *
 */
public class HdfStringApp {
    public static void main(String[] args) {
        new HdfStringApp().run();
    }

    private void run() {
        try {
            HdfFileReader reader = new HdfFileReader();
            String filePath = Objects.requireNonNull(HdfCompoundApp.class.getResource("/ascii_dataset.h5")).getFile();
            try (FileInputStream fis = new FileInputStream(filePath)) {
                FileChannel channel = fis.getChannel();
                reader.readFile(channel);
                tryStringSpliterator(channel, reader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            HdfFileReader reader = new HdfFileReader();
            String filePath = Objects.requireNonNull(HdfCompoundApp.class.getResource("/utf8_dataset.h5")).getFile();
            try (FileInputStream fis = new FileInputStream(filePath)) {
                FileChannel channel = fis.getChannel();
                reader.readFile(channel);
                tryStringSpliterator(channel, reader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            HdfFileReader reader = new HdfFileReader();
            String filePath = Objects.requireNonNull(HdfCompoundApp.class.getResource("/string_ascii_all.h5")).getFile();
            try (FileInputStream fis = new FileInputStream(filePath)) {
                FileChannel channel = fis.getChannel();
                reader.readFile(channel);
                tryStringSpliterator(channel, reader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            HdfFileReader reader = new HdfFileReader();
            String filePath = Objects.requireNonNull(HdfCompoundApp.class.getResource("/string_utf8_each.h5")).getFile();
            try (FileInputStream fis = new FileInputStream(filePath)) {
                FileChannel channel = fis.getChannel();
                reader.readFile(channel);
                tryStringSpliterator(channel, reader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        tryHdfApiStrings("string_ascii_all.h5", this::writeAll, StringDatatype.createClassBitField(StringDatatype.PaddingType.SPACE_PAD, StringDatatype.CharacterSet.ASCII), 8);
//        tryHdfApiStrings("string_utf8_each.h5", this::writeEach, StringDatatype.createClassBitField( StringDatatype.PaddingType.NULL_TERMINATE, StringDatatype.CharacterSet.UTF8), 12);
    }

    private void tryStringSpliterator(FileChannel fileChannel, HdfFileReader reader) throws IOException {

        TypedDataSource<String> dataSource = new TypedDataSource<>(reader.getDataSet(), fileChannel, reader.getDataAddress(), String.class);
        String[] allData = dataSource.readVector();
        System.out.println("String stream = " + Arrays.stream(allData).toList());
        System.out.println("String stream = " + dataSource.streamVector().toList());
        new TypedDataSource<>(reader.getDataSet(), fileChannel, reader.getDataAddress(), HdfString.class);
    }

    private void tryHdfApiStrings(String FILE_NAME, Consumer<WriterParams> writer, BitSet classBitField, int size) {
        final StandardOpenOption[] FILE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
        final String DATASET_NAME = "strings";
        final int NUM_RECORDS = 10;

        try {
            // Create a new HDF5 file
            HdfFile file = new HdfFile(FILE_NAME, FILE_OPTIONS);

            // Create data space
            HdfFixedPoint[] hdfDimensions = {HdfFixedPoint.of(NUM_RECORDS)};
            short dataSpaceMessageSize = 8;
            if ( hdfDimensions != null ) {
                for (HdfFixedPoint dimension : hdfDimensions) {
                    dataSpaceMessageSize += dimension.getDatatype().getSize();
                }
            }
            if ( hdfDimensions != null ) {
                for (HdfFixedPoint maxDimension : hdfDimensions) {
                    dataSpaceMessageSize += maxDimension.getDatatype().getSize();
                }
            }
            DataspaceMessage dataSpaceMessage = new DataspaceMessage(1, 1, 1, hdfDimensions, hdfDimensions, false, (byte)0, dataSpaceMessageSize);

            StringDatatype stringDatatype = new StringDatatype(
                    StringDatatype.createClassAndVersion(),
                    classBitField,
                    size);

            // Create dataset
            HdfDataSet dataset = file.createDataSet(DATASET_NAME, stringDatatype, dataSpaceMessage);

            HdfTestUtils.writeVersionAttribute(dataset);

            writer.accept(new HdfStringApp.WriterParams(NUM_RECORDS, stringDatatype, dataset));

            dataset.close();
            file.close();

            // auto close
            System.out.println("HDF5 file " + FILE_NAME + " created and written successfully!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private void writeEach(HdfStringApp.WriterParams writerParams) {
        AtomicInteger countHolder = new AtomicInteger(0);
        ByteBuffer byteBuffer = ByteBuffer.allocate(writerParams.stringDatatype.getSize());
        // Write to dataset
        writerParams.dataset.write(() -> {
            int count = countHolder.getAndIncrement();
            if (count >= writerParams.NUM_RECORDS) return ByteBuffer.allocate(0);
            byteBuffer.clear();
            byte[] bytes = ("ꦠꦤ꧀" + " " + (count+1)).getBytes();
            HdfString value = new HdfString(bytes, writerParams.stringDatatype);
            value.writeValueToByteBuffer(byteBuffer);
            byteBuffer.flip();
            return byteBuffer;
        });
    }

    @SneakyThrows
    private void writeAll(HdfStringApp.WriterParams writerParams) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(writerParams.stringDatatype.getSize() * writerParams.NUM_RECORDS);
        for(int i=0; i<writerParams.NUM_RECORDS; i++) {
            byte[] bytes = ("label " + (i + 1)).getBytes(StandardCharsets.US_ASCII);
            HdfString value = new HdfString(bytes, writerParams.stringDatatype);
            value.writeValueToByteBuffer(byteBuffer);
        }
        byteBuffer.flip();
        // Write to dataset
        writerParams.dataset.write(byteBuffer);
    }

    @AllArgsConstructor
    static class WriterParams {
        int NUM_RECORDS;
        StringDatatype stringDatatype;
        HdfDataSet dataset;
    }

}
