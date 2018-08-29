package com.eshishkin.edu.neural;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:shishkin.john@gmail.com">Eugene Shishkin</a>
 */
public class MnistHelper {

    private static final String TRAINING_SET_IMAGES = "train-images.idx3-ubyte";
    private static final String TRAINING_SET_LABELS = "train-labels.idx1-ubyte";
    private static final String VALIDATION_SET_IMAGES = "t10k-images.idx3-ubyte";
    private static final String VALIDATION_SET_LABELS = "t10k-labels.idx1-ubyte";

    public static final int IMAGE_SIZE = 28;

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

    private static final String MODE_READ_ONLY = "r";

    public static List<byte[]> loadTrainingImages() {
        return load(TRAINING_SET_IMAGES);
    }

    public static List<byte[]> loadTrainingLabels() {
        return load(TRAINING_SET_LABELS);
    }

    public static List<byte[]> loadValidationImages() {
        return load(VALIDATION_SET_IMAGES);
    }

    public static List<byte[]> loadValidationLabels() {
        return load(VALIDATION_SET_LABELS);
    }

    private static List<byte[]> load(String filename) {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            RandomAccessFile file = new RandomAccessFile(
                    new File(classloader.getResource(filename).toURI()),
                    MODE_READ_ONLY
            );

            int header = ByteBuffer.wrap(new byte[]{
                    file.readByte(),
                    file.readByte(),
                    file.readByte(),
                    file.readByte()
            }).order(ByteOrder.BIG_ENDIAN).getInt();

            int typeSize = getTypeLength(header);
            int n = getNumberOfDimensions(header);
            int metadataSize = HEADER_SIZE_IN_BYTES + n * DIMENSION_SIZE_IN_BYTES;
            long dataSize = file.length() - metadataSize;

            file.seek(HEADER_SIZE_IN_BYTES);
            byte[] dimensions = new byte[n * DIMENSION_SIZE_IN_BYTES];
            file.read(dimensions, 0, n * DIMENSION_SIZE_IN_BYTES);

            int[] sizes = getDimensions(n, dimensions);
            int oneElementSize = typeSize * flat(sizes);
            int numberOfElements = sizes[0];

            if (dataSize / oneElementSize != numberOfElements) {
                throw new RuntimeException("File is corrupted. Data is not complete");
            }

            List<byte[]> result = new LinkedList<>();

            for (int i = 0; i < numberOfElements; i++) {
                byte[] buffer = new byte[oneElementSize];
                file.seek(metadataSize + i * oneElementSize);
                file.read(buffer, 0, oneElementSize);
                result.add(buffer);
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load file", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to load file", e);
        }
    }

    private static int flat(int[] dimensions) {
        return Arrays.stream(dimensions).skip(1).reduce(1, (a, b) -> a * b);
    }

    private static int[] getDimensions(int n, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int[] dimensions = new int[n];

        for (int i = 0; i < n; i++) {
            dimensions[i] = buffer.getInt();
            System.out.println(dimensions[i]);
        }

        return dimensions;
    }

    private static int getTypeLength(int header) {
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

    private static int getNumberOfDimensions(int header) {
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