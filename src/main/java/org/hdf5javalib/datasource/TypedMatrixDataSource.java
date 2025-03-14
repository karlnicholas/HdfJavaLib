package org.hdf5javalib.datasource;

import org.hdf5javalib.dataclass.HdfFixedPoint;
import org.hdf5javalib.file.dataobject.HdfObjectHeaderPrefixV1;
import org.hdf5javalib.file.dataobject.message.DataspaceMessage;
import org.hdf5javalib.file.dataobject.message.DatatypeMessage;
import org.hdf5javalib.file.dataobject.message.datatype.FixedPointDatatype;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A data source for reading raw fixed-point data from HDF5 files into arrays of type T.
 * Optimized for bulk reading and streaming of raw data without mapping to a specific class.
 *
 * @param <T> the type of object returned by streaming or bulk reading operations
 */
public class TypedMatrixDataSource<T> extends AbstractTypedMatrixStreamingSource<T> {
    private final Class<T> dataClass;
//    private final HdfObjectHeaderPrefixV1 headerPrefixV1;
    private final int recordSize;
    private final int readsAvailable;
    private final int dimension;
    private final FixedPointDatatype fixedPointDatatype;
    /**
     * Constructs a DataClassDataSource for reading raw data from an HDF5 file.
     *
     * @param headerPrefixV1 the HDF5 object header prefix
     * @param scale the scale for BigDecimal values; 0 for BigInteger
     * @param fileChannel the FileChannel for streaming
     * @param startOffset the byte offset where the dataset begins
     * @param dataClass the Class object representing the type T
     * @throws IllegalStateException if metadata is missing
     * @throws IllegalArgumentException if dimensionality is unsupported
     */
    public TypedMatrixDataSource(HdfObjectHeaderPrefixV1 headerPrefixV1, int scale, FileChannel fileChannel, long startOffset, Class<T> dataClass) {
        super(headerPrefixV1, scale, fileChannel, startOffset);
        this.dataClass = dataClass;
        recordSize = headerPrefixV1.findMessageByType(DatatypeMessage.class).orElseThrow().getHdfDatatype().getSize();
        HdfFixedPoint[] dimensions = headerPrefixV1.findMessageByType(DataspaceMessage.class).orElseThrow().getDimensions();
        readsAvailable = dimensions[0].getInstance(Long.class).intValue();
        dimension = dimensions[1].getInstance(Long.class).intValue();
        fixedPointDatatype = (FixedPointDatatype) headerPrefixV1.findMessageByType(DatatypeMessage.class).orElseThrow().getHdfDatatype();
//        // Parse fields and map them to CompoundDatatype members
//        Field fieldToSet = null;
//        for (Field field : dataClass.getDeclaredFields()) {
//            field.setAccessible(true);
//            if( field.getName().equals(name)) {
//                fieldToSet = field;
//                break;
//            }
//        }
//        this.field = fieldToSet;

    }

    /**
     * Reads the entire dataset into an array of T objects in memory.
     *
     * @return an array of T containing all records
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if no FileChannel was provided
     */
    public T[][] readAll() throws IOException {
        if (fileChannel == null) {
            throw new IllegalStateException("Reading all data requires a FileChannel; use the appropriate constructor.");
        }
        long totalSize = sizeForReadBuffer * readsAvailable;
        if (totalSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("Dataset size exceeds maximum array capacity: " + totalSize);
        }

        ByteBuffer buffer = ByteBuffer.allocate((int) totalSize).order(ByteOrder.LITTLE_ENDIAN);
        synchronized (fileChannel) {
            fileChannel.position(startOffset);
            int totalBytesRead = 0;
            while (totalBytesRead < totalSize) {
                int bytesRead = fileChannel.read(buffer);
                if (bytesRead == -1) {
                    throw new IOException("Unexpected EOF after " + totalBytesRead + " bytes; expected " + totalSize);
                }
                totalBytesRead += bytesRead;
            }
        }
        buffer.flip();

        @SuppressWarnings("unchecked")

        T[][] result = (T[][]) Array.newInstance(dataClass, readsAvailable, dimension); // Create matrix of type T
        for (int i = 0; i < readsAvailable; i++) {
            result[i] = populateFromBufferRaw(buffer);
        }
        return result;
    }

    @Override
    public Stream<T[]> stream() {
        if (fileChannel == null) {
            throw new IllegalStateException("Streaming requires a FileChannel; use the appropriate constructor.");
        }
        return StreamSupport.stream(new DataClassSpliterator(startOffset, endOffset), false);
    }

    @Override
    public Stream<T[]> parallelStream() {
        if (fileChannel == null) {
            throw new IllegalStateException("Streaming requires a FileChannel; use the appropriate constructor.");
        }
        return StreamSupport.stream(new DataClassSpliterator(startOffset, endOffset), true);
    }

    @Override
    protected T[] populateFromBufferRaw(ByteBuffer buffer) {
        @SuppressWarnings("unchecked")
        T[] data = (T[]) Array.newInstance(dataClass, dimension); // Create array of type T
        for(int i = 0; i < dimension; i++) {
            data[i] = (T) fixedPointDatatype.getInstance(dataClass, buffer);
        }
//        T[] result = (T[]) datatype.getInstance(buffer);
        return data;
    }
}