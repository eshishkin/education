package com.eshishkin.edu.neural;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author <a href="mailto:shishkin.john@gmail.com">Eugene Shishkin</a>
 */
public class MNISTDataLoader {

//    0x08: unsigned byte
//    0x09: signed byte
//    0x0B: short (2 bytes)
//    0x0C: int (4 bytes)
//    0x0D: float (4 bytes)
//    0x0E: double (8 bytes)

    private static final int TYPE_BYTE = 0x0800;
    private static final int TYPE_SIGNED_BYTE = 0x0900;
    private static final int TYPE_SHORT = 0x0B00;
    private static final int TYPE_INT = 0x0C00;
    private static final int TYPE_FLOAT = 0x0D00;
    private static final int TYPE_DOUBLE = 0x0E00;

    private static final int HEADER_SIZE_IN_BYTES = 4;
    private static final int DIMENSION_SIZE_IN_BYTES = 4;
//
//    public void load(byte[] data) {
//        int header = ByteBuffer.wrap(data, 0, HEADER_SIZE_IN_BYTES).order(ByteOrder.BIG_ENDIAN).getInt();
//
//        int typeSize = getTypeLength(header);
//        int n = getNumberOfDimensions(header);
//
//        int[] dimensions = getDimensions(
//                n,
//                Arrays.copyOfRange(data, HEADER_SIZE_IN_BYTES, HEADER_SIZE_IN_BYTES + n * DIMENSION_SIZE_IN_BYTES)
//        );
//
//        int metadataSize = HEADER_SIZE_IN_BYTES + n * DIMENSION_SIZE_IN_BYTES;
//        int dataSize = data.length - metadataSize;
//        int oneElementSize = typeSize * flat(dimensions);
//        int numberOfElements = dimensions[0];
//
//        if (dataSize / oneElementSize != numberOfElements) {
//            throw new RuntimeException("File is corrupted. Data is not complete");
//        }
//        List<List<Number>> result = new ArrayList<>();
//        for (int i = 0; i< numberOfElements; i++) {
//            ByteBuffer b = ByteBuffer.wrap(data, metadataSize + i * oneElementSize, oneElementSize);
//
//            for (int j = 0; j < flat(dimensions); j++) {
//            }
//        }
//
////        Stream.generate(new Supplier<List<Number>>() {
////            @Override
////            public List<Number> get() {
////                return null;
////            }
////        }).
//    }

    public void load(RandomAccessFile file) throws IOException {

        int header = ByteBuffer.wrap(new byte[] {file.readByte(), file.readByte(), file.readByte(), file.readByte()})
                .order(ByteOrder.BIG_ENDIAN).getInt();

        int typeSize = getTypeLength(header);
        int n = getNumberOfDimensions(header);
        int metadataSize = HEADER_SIZE_IN_BYTES + n * DIMENSION_SIZE_IN_BYTES;
        long dataSize = file.length() - metadataSize;

        byte[] dimensions = new byte[metadataSize];
        file.read(dimensions, HEADER_SIZE_IN_BYTES, metadataSize);

        int[] sizes = getDimensions(n, dimensions);
        int oneElementSize = typeSize * flat(sizes);
        int numberOfElements = sizes[0];

        if (dataSize / oneElementSize != numberOfElements) {
            throw new RuntimeException("File is corrupted. Data is not complete");
        }



        for (int i = 0; i< numberOfElements; i++) {
            byte[] buffer = new byte[oneElementSize];
            file.read(buffer, metadataSize + i * oneElementSize, oneElementSize);
        }
    }

    private int flat(int[] dimensions) {
        return Arrays.stream(dimensions).skip(1).reduce(1, (a, b) -> a * b);
    }

    private int[] getDimensions(int n, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int[] dimensions = new int[n];

        for (int i = 0; i < n; i++) {
            dimensions[i] = buffer.getInt();
            System.out.println(dimensions[i]);
        }

        return dimensions;
    }

    private int getTypeLength(int header) {
        switch (header & 0xFF00) {
            case TYPE_BYTE:
            case TYPE_SIGNED_BYTE:
                return 1;
            case TYPE_SHORT:
                return 2;
            case TYPE_INT:
            case TYPE_FLOAT:
                return 4;
            case TYPE_DOUBLE:
                return 8;
            default:
                throw new RuntimeException("Unparsable header: " + Integer.toHexString(header));
        }
    }

    private int getNumberOfDimensions(int header) {
        switch (header & 0xFF) {
            case 0x01:
                return 1;
            case 0x02:
                return 2;
            case 0x03:
                return 3;
            default:
                throw new RuntimeException("Dimension more than 3 is not supported now");
        }
    }
}