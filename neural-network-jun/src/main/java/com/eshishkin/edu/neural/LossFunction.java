package com.eshishkin.edu.neural;

import org.la4j.Vector;

/**
 * @author <a href="mailto:shishkin.john@gmail.com">Eugene Shishkin</a>
 */
public interface LossFunction {

    Vector derivative(Vector output, Vector expected);

    public static class QuadricLossFunction implements LossFunction {

        public Vector derivative(Vector activated, Vector expected) {
            return activated.add(expected.multiply(-1));
        }
    }

    public static class CrossEntropyLossFunction implements LossFunction {

        public Vector derivative(Vector activated, Vector expected) {
//            ToDo ES: implement
            return null;
        }
    }
}
