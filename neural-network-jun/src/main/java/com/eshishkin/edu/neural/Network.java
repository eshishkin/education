package com.eshishkin.edu.neural;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.la4j.Matrix;
import org.la4j.Vector;

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

    public void educate(EducationPlan plan) {
        SGD(plan);
    }

    public void SGD(EducationPlan plan) {
        int batch = 1;
        double kk = plan.getEducationRate() / batch;

        for (int i = 0; i < plan.getEpochs(); i++) {

            LayerGradientHolder gradients = LayerGradientHolder.fromSum(
                    plan.forEachTrainingEntry(this::grad)
            );

            for (int k = 0; k < layers; k++) {
                Matrix nabla_w = gradients.getGradientByWeight(k).multiply(-kk);
                Vector nabla_b = gradients.getGradientByBias(k).multiply(-kk);

                weights.set(k, weights.get(k).add(nabla_w));
                bias.set(k, bias.get(k).add(nabla_b));
            }
        }
    }

    private LayerGradientHolder grad(Vector initial, Vector expected) {
        List<Vector> w_sums = new ArrayList<>();
        List<Vector> activations = new ArrayList<>();

        Vector w_sum = null;

        Vector activation = initial;
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


        LayerGradientHolder holder = new LayerGradientHolder();

        Vector nabla_b = delta;
        Matrix nabla_w = delta.toColumnMatrix().multiply(activations.get(layers - 1).toRowMatrix());

        holder.addToHead(nabla_w, nabla_b);
        for (int i = layers - 1; i > 0; i--) {
            Vector z = w_sums.get(i - 1);

            delta = weights.get(i)
                    .transpose()
                    .multiply(delta)
                    .hadamardProduct(z.transform((k, v) -> sigmoidDerivative(v)));

            nabla_b = delta;

            nabla_w = delta.toColumnMatrix().multiply(activations.get(i - 1).toRowMatrix());
            holder.addToHead(nabla_w, nabla_b);
        }

        return holder;
    }

    private Vector costDerivative(Vector activated, Vector expected) {
        return activated.add(expected.multiply(-1));
    }

//        ToDo ES: implement
//    private List<Pair<Vector, Vector>> takeRandom(List<Pair<Vector, Vector>> data, int batch) {
//        return data;
//    }

    private double sigmoid(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    private double sigmoidDerivative(double z) {
        return sigmoid(z) * (1 - sigmoid(z));
    }
}
