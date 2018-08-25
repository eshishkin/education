package com.eshishkin.edu.neural;

import java.util.Arrays;

import org.la4j.Matrix;
import org.la4j.Vector;

import javafx.util.Pair;

/**
 * @author <a href="mailto:shishkin.john@gmail.com">Eugene Shishkin</a>
 */
public class Main {
    public static void main(String[] arg) {
        Network network = new Network(Arrays.asList(2, 4, 1));

        network.gradientDecent(Arrays.asList(
                new Pair<>(Vector.fromArray(new double[] {1 ,0}), Vector.fromArray(new double[] {1})),
                new Pair<>(Vector.fromArray(new double[] {1 ,1}), Vector.fromArray(new double[] {0})),
                new Pair<>(Vector.fromArray(new double[] {0 ,1}), Vector.fromArray(new double[] {1})),
                new Pair<>(Vector.fromArray(new double[] {0 ,0}), Vector.fromArray(new double[] {0}))
        ), 0.5, 5000);

        System.out.println("Evaluation for (1,1):" + network.evaluate(Arrays.asList(1D, 1D)));
        System.out.println("Evaluation for (0,0):" + network.evaluate(Arrays.asList(0D, 0D)));
        System.out.println("Evaluation for (1,0):" + network.evaluate(Arrays.asList(1D, 0D)));
        System.out.println("Evaluation for (0,1):" + network.evaluate(Arrays.asList(0D, 1D)));
    }
}
