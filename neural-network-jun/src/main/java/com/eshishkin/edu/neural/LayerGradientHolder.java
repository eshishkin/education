package com.eshishkin.edu.neural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.la4j.Matrix;
import org.la4j.Vector;

import static java.util.stream.Collectors.toList;

/**
 * Holder class that hold layer by layer gradients by weighs and biases.
 *
 * @author <a href="mailto:shishkin.john@gmail.com">Eugene Shishkin</a>
 */
public class LayerGradientHolder {

    public static final String COULD_NOT_SUMMARIZE_GRADIENTS = "Could not summarize gradients";
    private int layers;
    private List<Matrix> gradientByWeight = new ArrayList<>();
    private List<Vector> gradientByBias = new ArrayList<>();

    public static LayerGradientHolder fromSum(List<LayerGradientHolder> gradients) {
        if (gradients.size() > 1) {
            return gradients.get(0).sum(gradients.stream().skip(0).collect(toList()));
        } else {
            return gradients.get(0);
        }
    }

    public LayerGradientHolder() {
    }

    private LayerGradientHolder(List<Matrix> gradientByWeight, List<Vector> gradientByBias) {
        this.gradientByWeight = gradientByWeight;
        this.gradientByBias = gradientByBias;
        this.layers = gradientByWeight.size();
    }

    public LayerGradientHolder addToTail(Matrix byWeight, Vector byBias) {
        gradientByWeight.add(byWeight);
        gradientByBias.add(byBias);
        layers++;
        return this;
    }

    public LayerGradientHolder addToHead(Matrix byWeight, Vector byBias) {
        gradientByWeight.add(0, byWeight);
        gradientByBias.add(0, byBias);
        layers++;
        return this;
    }

    public LayerGradientHolder sum(LayerGradientHolder g) {
        return sum(Arrays.asList(g));
    }

    public LayerGradientHolder sum(List<LayerGradientHolder> gradients) {
        validate(gradients);

        List<Matrix> total_nabla_w = new ArrayList<>();
        List<Vector> total_nabla_b = new ArrayList<>();

        for (int i = 0; i < layers; i++) {
            final int k = i;

            Optional<Matrix> nabla_w = Stream.concat(
                    Stream.of(gradientByWeight.get(k)),
                    gradients.stream().map(g -> g.getGradientByWeight().get(k))
            ).reduce((a, b) -> a.add(b));

            Optional<Vector> nabla_b = Stream.concat(
                    Stream.of(gradientByBias.get(k)),
                    gradients.stream().map(g -> g.getGradientByBias().get(k))
            ).reduce((a, b) -> a.add(b));

            total_nabla_w.add(
                    nabla_w.orElseThrow(() -> new RuntimeException(COULD_NOT_SUMMARIZE_GRADIENTS))
            );
            total_nabla_b.add(
                    nabla_b.orElseThrow(() -> new RuntimeException(COULD_NOT_SUMMARIZE_GRADIENTS))
            );
        }
        return new LayerGradientHolder(total_nabla_w, total_nabla_b);
    }


    public List<Matrix> getGradientByWeight() {
        return gradientByWeight;
    }

    public List<Vector> getGradientByBias() {
        return gradientByBias;
    }


    public Matrix getGradientByWeight(int i) {
        return gradientByWeight.get(i);
    }

    public Vector getGradientByBias(int i) {
        return gradientByBias.get(i);
    }

    private void validate(List<LayerGradientHolder> gradients) {
        for (int i = 0; i < layers; i++) {
            final int k = i;

            Matrix m_e = getGradientByWeight(k);


            boolean sameSizeForWeights = gradients.stream()
                    .map(x -> x.getGradientByWeight(k))
                    .allMatch(m -> m.rows() == m_e.rows() && m.columns() == m_e.columns());

            boolean sameSizeForBiases = gradients.stream()
                    .map(x -> x.getGradientByBias(k))
                    .allMatch(m -> m.length() == getGradientByBias(k).length());

            if (!sameSizeForWeights) {
                throw new RuntimeException(
                        "Could not sum gradients by weights since matrix have different size"
                );
            }

            if (!sameSizeForBiases) {
                throw new RuntimeException(
                        "Could not sum gradients by biases since vectors have different size"
                );
            }
        }
    }
}
