package com.eshishkin.edu.neural;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.la4j.Matrix;
import org.la4j.Vector;

import javafx.util.Pair;

/**
 * @author <a href="mailto:shishkin.john@gmail.com">Eugene Shishkin</a>
 */
public class Network {

    private int layers;
    private List<Integer> sizes;
    private List<Matrix> weights;
    private List<Vector> bias;

    public Network(List<Integer> sizes) {
        this.layers = sizes.size() - 1;
        this.sizes = sizes;
        this.weights = new ArrayList<>(layers - 1);
        this.bias = new ArrayList<>(layers - 1);

        for (int i = 0; i < layers; i++) {
            int currentSize = this.sizes.get(i);
            int nextSize = this.sizes.get(i + 1);
            this.weights.add(i, Matrix.random(nextSize, currentSize, new Random()));
            this.bias.add(i, Vector.random(nextSize, new Random()));
        }
    }

    public Vector evaluate(Vector data) {
        Vector result = data;

        for (int i = 0; i < layers; i++) {
            result = weights
                    .get(i)
                    .multiply(result)
                    .add(bias.get(i))
                    .transform((index, value) -> sigmoid(value));
        }

        return result;
    }

    public Vector evaluate(List<Double> data) {
        return evaluate(Vector.fromCollection(data));
    }

    public void gradientDecent(List<Pair<Vector, Vector>> training_data,
                                double rate,
                                int epoch) {
        int batch = 1;
        for (int i = 0; i < epoch; i++) {
            double kk = rate / batch;
            List<Pair<Matrix, Vector>> gradients = takeRandom(training_data, batch)
                    .stream()
                    .map(this::grad)
                    .reduce(new BinaryOperator<List<Pair<Matrix, Vector>>>() {
                        @Override
                        public List<Pair<Matrix, Vector>> apply(List<Pair<Matrix, Vector>> pairs,
                                                                List<Pair<Matrix, Vector>> pairs2) {
                            List<Pair<Matrix, Vector>> result = new ArrayList<Pair<Matrix, Vector>>();
                            for (int j = 0; j < pairs.size(); j++) {
                                result.add(new Pair<Matrix, Vector>(
                                        pairs.get(j).getKey().add(pairs2.get(j).getKey()),
                                        pairs.get(j).getValue().add(pairs2.get(j).getValue())
                                ));
                            }
                            return result;
                        }
                    })
                    .map(g -> {
                        List<Pair<Matrix, Vector>> result = new ArrayList<Pair<Matrix, Vector>>();
                        for (int j = 0; j < g.size(); j++) {
                            result.add(new Pair<Matrix, Vector>(
                                    g.get(j).getKey().multiply(kk),
                                    g.get(j).getValue().multiply(kk)
                            ));
                        }
                        return result;
                    })
                    .orElseThrow(new Supplier<RuntimeException>() {
                        @Override
                        public RuntimeException get() {
                            return new RuntimeException("olololo");
                        }
                    });

            for (int k = 0; k < layers; k++) {
                final Matrix nabla_w = gradients.get(k).getKey();
                final Vector nabla_b = gradients.get(k).getValue();

                weights.set(
                        k,
                        weights.get(k).add(nabla_w.multiply(-1))
                );
                bias.set(k, bias.get(k).add(nabla_b.multiply(-1)));
            }
        }
    }

    private List<Pair<Matrix, Vector>> grad(Pair<Vector, Vector> training_data) {
        Vector expected = training_data.getValue();
        List<Vector> w_sums = new ArrayList<>();
        List<Vector> activations = new ArrayList<>();

        Vector w_sum = null;

        Vector activation = training_data.getKey();
        activations.add(activation);

        for (int i = 0; i < layers; i++) {
            w_sum = weights
                    .get(i)
                    .multiply(activation)
                    .add(bias.get(i));

            activation = w_sum.transform((index, value) -> sigmoid(value));

            w_sums.add(w_sum);
            activations.add(activation);
        }

        Vector delta = w_sum
                .transform((i, v) -> sigmoidDerivative(v))
                .hadamardProduct(costDerivative(activation, expected));

        List<Pair<Matrix, Vector>> grad = new ArrayList<>();

        Vector nabla_b = delta;
        Matrix nabla_w = delta.toColumnMatrix().multiply(activations.get(layers - 1).toRowMatrix());

        grad.add(new Pair<Matrix, Vector>(nabla_w, nabla_b));
        for (int i = layers - 1; i > 0; i--) {
            Vector z = w_sums.get(i - 1);

            delta = weights.get(i)
                    .transpose()
                    .multiply(delta)
                    .hadamardProduct(z.transform((k, v) -> sigmoidDerivative(v)));

            nabla_b = delta;

            nabla_w = delta.toColumnMatrix().multiply(activations.get(i - 1).toRowMatrix());
            grad.add(0, new Pair<Matrix, Vector>(nabla_w, nabla_b));
        }

        return grad;
    }


    private Vector costDerivative(Vector activated, Vector expected) {
        return activated.add(expected.multiply(-1));
    }
    private List<Pair<Vector, Vector>> takeRandom(List<Pair<Vector, Vector>> data, int batch) {
//        ToDo ES: implement
        return data;
    }

    private double sigmoid(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    private double sigmoidDerivative(double z) {
        return sigmoid(z) * (1 - sigmoid(z));
    }
}
