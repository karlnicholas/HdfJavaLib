package com.github.karlnicholas.hdf5javalib.utils;

import com.github.karlnicholas.hdf5javalib.data.FixedPointMatrixSource;
import com.github.karlnicholas.hdf5javalib.data.FixedPointVectorSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Spliterator;
import java.util.function.Consumer;

public class HdfFixedPointVectorSpliterator<T> implements Spliterator<T> {
    private final FileChannel fileChannel;
    private final long sizeForReadBuffer;
    private final long endOffset;
    private long currentOffset;
    private final FixedPointVectorSource<T> fixedPointVectorSource;

    public HdfFixedPointVectorSpliterator(FileChannel fileChannel, long startOffset, FixedPointVectorSource<T> fixedPointVectorSource) {
        this.fileChannel = fileChannel;
        this.sizeForReadBuffer = fixedPointVectorSource.getSizeForReadBuffer();
        this.currentOffset = startOffset;
        this.endOffset = startOffset + sizeForReadBuffer * fixedPointVectorSource.getNumberOfReadsAvailable();
        this.fixedPointVectorSource = fixedPointVectorSource;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (currentOffset >= endOffset) {
            return false;
        }

        try {
            ByteBuffer buffer = ByteBuffer.allocate((int) sizeForReadBuffer).order(ByteOrder.LITTLE_ENDIAN);
            fileChannel.position(currentOffset);
            int bytesRead = fileChannel.read(buffer);

            if (bytesRead == -1 || bytesRead < sizeForReadBuffer) {
                return false;
            }

            buffer.flip();

            // Use the prototype to populate a new instance
            T dataSource = fixedPointVectorSource.populateFromBuffer(buffer);

            action.accept(dataSource);

            currentOffset += sizeForReadBuffer;
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error processing HDF data", e);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
//        long remainingRecords = (endOffset - currentOffset) / sizeForReadBuffer;
//        if (remainingRecords <= 1) {
//            return null;
//        }
//
//        long midpoint = currentOffset + (remainingRecords / 2) * sizeForReadBuffer;
//        Spliterator<T> newSpliterator = new HdfFixedPointVectorSpliterator<>(
//                fileChannel, currentOffset, sizeForReadBuffer, (midpoint - currentOffset) / sizeForReadBuffer, fixedPointVectorSource
//        );
//        currentOffset = midpoint;
//        return newSpliterator;
        return null;
    }

    @Override
    public long estimateSize() {
        return (endOffset - currentOffset) / sizeForReadBuffer;
    }

    @Override
//    public int characteristics() {
//        return NONNULL | ORDERED | IMMUTABLE | SIZED | SUBSIZED;
//    }
    public int characteristics() {
        return NONNULL | ORDERED | IMMUTABLE | SIZED;
    }
}
