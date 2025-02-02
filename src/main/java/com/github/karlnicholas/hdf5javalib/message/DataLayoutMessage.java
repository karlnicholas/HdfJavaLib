package com.github.karlnicholas.hdf5javalib.message;

import com.github.karlnicholas.hdf5javalib.datatype.HdfFixedPoint;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@Getter
public class DataLayoutMessage extends HdfMessage {
    private final int version;
    private final int layoutClass;
    private final HdfFixedPoint dataAddress;
    private final HdfFixedPoint[] dimensionSizes;
    private final int compactDataSize;
    private final byte[] compactData;
    private final HdfFixedPoint datasetElementSize;

    // Constructor to initialize all fields
    public DataLayoutMessage(
            int version,
            int layoutClass,
            HdfFixedPoint dataAddress,
            HdfFixedPoint[] dimensionSizes,
            int compactDataSize,
            byte[] compactData,
            HdfFixedPoint datasetElementSize
    ) {
        super((short) 8, ()->{
            short size = (short) (1+1+dataAddress.getSizeMessageData());
            for (HdfFixedPoint dimensionSize : dimensionSizes) {
                size += dimensionSize.getSizeMessageData();
            }
            size += 2;
            size += datasetElementSize == null ? 0: datasetElementSize.getSizeMessageData();
            return size;
        }, (byte)0);
        this.version = version;
        this.layoutClass = layoutClass;
        this.dataAddress = dataAddress;
        this.dimensionSizes = dimensionSizes;
        this.compactDataSize = compactDataSize;
        this.compactData = compactData;
        this.datasetElementSize = datasetElementSize;
    }

    /**
     * Parses the header message and returns a constructed instance.
     *
     * @param flags      Flags associated with the message (not used here).
     * @param data       Byte array containing the header message data.
     * @param offsetSize Size of offsets in bytes.
     * @param lengthSize Size of lengths in bytes (not used here).
     * @return A fully constructed `DataLayoutMessage` instance.
     */
    public static HdfMessage parseHeaderMessage(byte flags, byte[] data, short offsetSize, short lengthSize) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // Read version (1 byte)
        int version = Byte.toUnsignedInt(buffer.get());
        if (version < 1 || version > 3) {
            throw new IllegalArgumentException("Unsupported Data Layout Message version: " + version);
        }

        // Read layout class (1 byte)
        int layoutClass = Byte.toUnsignedInt(buffer.get());

        // Initialize fields
        HdfFixedPoint dataAddress = null;
        HdfFixedPoint[] dimensionSizes = null;
        int compactDataSize = 0;
        byte[] compactData = null;
        HdfFixedPoint datasetElementSize = null;

        // Parse based on layout class
        switch (layoutClass) {
            case 0: // Compact Storage
                compactDataSize = Short.toUnsignedInt(buffer.getShort()); // Compact data size (2 bytes)
                compactData = new byte[compactDataSize];
                buffer.get(compactData); // Read compact data
                break;

            case 1: // Contiguous Storage
                dataAddress = HdfFixedPoint.readFromByteBuffer(buffer, offsetSize, false); // Data address
                dimensionSizes = new HdfFixedPoint[1];
                dimensionSizes[0] = HdfFixedPoint.readFromByteBuffer(buffer, offsetSize, false); // Dimension size
                break;

            case 2: // Chunked Storage
                dataAddress = HdfFixedPoint.readFromByteBuffer(buffer, offsetSize, false); // Data address
                int numDimensions = Byte.toUnsignedInt(buffer.get()); // Number of dimensions (1 byte)
                dimensionSizes = new HdfFixedPoint[numDimensions];
                for (int i = 0; i < numDimensions; i++) {
                    dimensionSizes[i] = HdfFixedPoint.readFromByteBuffer(buffer, offsetSize, false); // Dimension sizes
                }
                datasetElementSize = HdfFixedPoint.readFromByteBuffer(buffer, (short)4, false); // Dataset element size (4 bytes)
                break;

            default:
                throw new IllegalArgumentException("Unsupported layout class: " + layoutClass);
        }

        // Return a constructed instance of DataLayoutMessage
        return new DataLayoutMessage(version, layoutClass, dataAddress, dimensionSizes, compactDataSize, compactData, datasetElementSize);
    }

    @Override
    public String toString() {
        return "DataLayoutMessage{" +
                "version=" + version +
                ", layoutClass=" + layoutClass +
                ", dataAddress=" + (layoutClass == 1 || layoutClass == 2 ? dataAddress : "N/A") +
                ", dimensionSizes=" + Arrays.toString(dimensionSizes) +
                ", compactDataSize=" + (layoutClass == 0 ? compactDataSize : "N/A") +
                ", compactData=" + (layoutClass == 0 ? Arrays.toString(compactData) : "N/A") +
                ", datasetElementSize=" + (layoutClass == 2 ? datasetElementSize : "N/A") +
                '}';
    }

    @Override
    public void writeToByteBuffer(ByteBuffer buffer, int offsetSize) {
        writeMessageData(buffer);

    }
}
