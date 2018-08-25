package com.eshishkin.edu.neural;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.la4j.Vector;

/**
 * @author <a href="mailto:shishkin.john@gmail.com">Eugene Shishkin</a>
 */
public class EducationPlan {

    private int epochs = 1000;
    private double educationRate = 0.5;
    private List<Pair<Vector, Vector>> trainingSet = new ArrayList<>();

    private EducationPlan(int epochs, double educationRate,
                          List<Pair<Vector, Vector>> trainingSet) {
        this.epochs = epochs;
        this.educationRate = educationRate;
        this.trainingSet = trainingSet;
    }

    public int getEpochs() {
        return epochs;
    }

    public double getEducationRate() {
        return educationRate;
    }

    public <T> List<T> forEachTrainingEntry(BiFunction<Vector, Vector, T> consumer) {
        return trainingSet
                .stream()
                .map(p -> consumer.apply(p.getLeft(), p.getRight()))
                .collect(Collectors.toList());
    }

    public void forEachTrainingEntry(BiConsumer<Vector, Vector> consumer) {
        trainingSet.forEach(p -> consumer.accept(p.getLeft(), p.getRight()));
    }


    public static class EducationPlanBuilder {
        private int epochs = 1000;
        private double educationRate = 1;
        private List<Pair<Vector, Vector>> trainingSet = new ArrayList<>();

        public static EducationPlanBuilder of() {
            return new EducationPlanBuilder();
        }

        public EducationPlanBuilder withEpochs(int epochs) {
            this.epochs = epochs;
            return this;
        }

        public EducationPlanBuilder withEducationRate(double rate) {
            this.educationRate = rate;
            return this;
        }

        public EducationPlanBuilder withTrainingData(Vector initial, Vector expected) {
            this.trainingSet.add(Pair.of(initial, expected));
            return this;
        }

        public EducationPlanBuilder withTrainingData(double[] initial, double[] expected) {
            this.trainingSet.add(Pair.of(
                    Vector.fromArray(initial), Vector.fromArray(expected)
            ));
            return this;
        }

        public EducationPlanBuilder withTrainingData(List<Double> initial, List<Double> expected) {
            this.trainingSet.add(Pair.of(
                    Vector.fromCollection(initial),
                    Vector.fromCollection(expected)
            ));
            return this;
        }

        public EducationPlan build() {
            return new EducationPlan(epochs, educationRate, trainingSet);
        }
    }
}
