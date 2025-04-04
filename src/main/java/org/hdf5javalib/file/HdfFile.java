package org.hdf5javalib.file;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hdf5javalib.dataclass.HdfFixedPoint;
import org.hdf5javalib.file.dataobject.message.DataLayoutMessage;
import org.hdf5javalib.file.dataobject.message.DataspaceMessage;
import org.hdf5javalib.file.dataobject.message.datatype.HdfDatatype;
import org.hdf5javalib.file.infrastructure.HdfGlobalHeap;
import org.hdf5javalib.file.infrastructure.HdfSymbolTableEntry;
import org.hdf5javalib.file.metadata.HdfSuperblock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

@Getter
@Slf4j
public class HdfFile {
    private final String fileName;
    private final StandardOpenOption[] openOptions;
    // initial setup without Dataset
    private final HdfSuperblock superblock;
    private final HdfGroup rootGroup;
    private final HdfBufferAllocation bufferAllocation;
    private final HdfGlobalHeap globalHeap;

    public HdfFile(String fileName, StandardOpenOption[] openOptions) {
        this.fileName = fileName;
        this.openOptions = openOptions;
        this.bufferAllocation = new HdfBufferAllocation();
        this.globalHeap = new HdfGlobalHeap(bufferAllocation::getGlobalHeapAddress);

        // 100320
        superblock = new HdfSuperblock(0, 0, 0, 0,
                (short)8, (short)8,
                4, 16,
                HdfFixedPoint.of(0),
                HdfFixedPoint.undefined((short)8),
                HdfFixedPoint.of(bufferAllocation.getDataAddress()),
                HdfFixedPoint.undefined((short)8),
                new HdfSymbolTableEntry(
                        HdfFixedPoint.of(0),
                        HdfFixedPoint.of(bufferAllocation.getObjectHeaderPrefixAddress()),
                        HdfFixedPoint.of(bufferAllocation.getBtreeAddress()),
                        HdfFixedPoint.of(bufferAllocation.getLocalHeapAddress())));

        rootGroup = new HdfGroup(this, "", bufferAllocation.getBtreeAddress(), bufferAllocation.getLocalHeapAddress());
    }

    /**
     * by default, the root group.
     * @param datasetName String
     * @param hdfDatatype HdfDatatype
     * @param dataSpaceMessage DataspaceMessage
     * @return HdfDataSet
     */
    public HdfDataSet createDataSet(String datasetName, HdfDatatype hdfDatatype, DataspaceMessage dataSpaceMessage) {
        hdfDatatype.setGlobalHeap(globalHeap);
        return rootGroup.createDataSet(datasetName, hdfDatatype, dataSpaceMessage, bufferAllocation.getDataGroupAddress());
    }

    protected void recomputeGlobalHeapAddress(HdfDataSet dataSet) {
        HdfFixedPoint dimensionSize = dataSet.getDataObjectHeaderPrefix().findMessageByType(DataLayoutMessage.class).orElseThrow().getDimensionSizes()[0];
        bufferAllocation.computeGlobalHeapAddress(dimensionSize.getInstance(Long.class));
    }

    public long write(Supplier<ByteBuffer> bufferSupplier) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(Path.of(fileName), openOptions)) {
            fileChannel.position(bufferAllocation.getDataAddress());
            ByteBuffer buffer;
            while ((buffer = bufferSupplier.get()).hasRemaining()) {
                while (buffer.hasRemaining()) {
                    fileChannel.write(buffer);
                }
            }
        }
        return bufferAllocation.getDataAddress();
    }

    public long write(ByteBuffer buffer, HdfDataSet hdfDataSet) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(Path.of(fileName), openOptions)) {
            fileChannel.position(bufferAllocation.getDataAddress());
            while (buffer.hasRemaining()) {
                fileChannel.write(buffer);
            }
        }
        return bufferAllocation.getDataAddress();
    }

    public void close() throws IOException {
        long endOfFileAddress = bufferAllocation.getDataAddress();
        HdfFixedPoint[] dimensionSizes = rootGroup.getDataSet().getDataObjectHeaderPrefix()
                .findMessageByType(DataLayoutMessage.class)
                .orElseThrow()
                .getDimensionSizes();
        for(HdfFixedPoint fixedPoint : dimensionSizes) {
            endOfFileAddress += fixedPoint.getInstance(Long.class);
        }

        // some convoluted logic for adding globalHeap data if needed
        ByteBuffer globalHeapBuffer = null;
        long globalHeapAddress = -1;
        long globalHeapSize = globalHeap.getWriteBufferSize();
        if ( globalHeapSize > 0 ) {
            globalHeapAddress = endOfFileAddress;
            endOfFileAddress += globalHeapSize;
            globalHeapBuffer = ByteBuffer.allocate((int) globalHeapSize);
            globalHeap.writeToByteBuffer(globalHeapBuffer);
            globalHeapBuffer.position(0);
        }
        superblock.setEndOfFileAddress(HdfFixedPoint.of(endOfFileAddress));


        log.debug("{}", superblock);
        log.debug("{}", rootGroup);

        // Allocate the buffer dynamically up to the data start location
        ByteBuffer buffer = ByteBuffer.allocate((int) bufferAllocation.getDataAddress()).order(ByteOrder.LITTLE_ENDIAN); // HDF5 uses little-endian
        buffer.position((int) bufferAllocation.getSuperblockAddress());
        superblock.writeToByteBuffer(buffer);
        buffer.position((int) bufferAllocation.getObjectHeaderPrefixAddress());
        rootGroup.writeToBuffer(buffer);
        buffer.position(0);

        Path path = Path.of(fileName);
        StandardOpenOption[] fileOptions = {StandardOpenOption.WRITE};
        if ( !Files.exists(path) ) {
            fileOptions =new StandardOpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE};
        }
        try (FileChannel fileChannel = FileChannel.open(path, fileOptions)) {
            fileChannel.position(0);

            while (buffer.hasRemaining()) {
                fileChannel.write(buffer);
            }
            // check here if global heap needs to be written
            if ( globalHeapAddress > 0 ) {
                fileChannel.position(globalHeapAddress);
                while (globalHeapBuffer.hasRemaining()) {
                    fileChannel.write(globalHeapBuffer);
                }
            }
        }
    }

}
