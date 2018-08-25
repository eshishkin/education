package com.eshishkin.edu.neural;

import java.util.Arrays;

/**
 * @author <a href="mailto:shishkin.john@gmail.com">Eugene Shishkin</a>
 */
public class Main {
    public static void main(String[] arg) {
        Network network = new Network(Arrays.asList(2, 4, 1));

        EducationPlan plan = EducationPlan.EducationPlanBuilder.of()
                .withEpochs(5000)
                .withEducationRate(0.5)
                .withTrainingData(new double[]{0, 0}, new double[]{0})
                .withTrainingData(new double[]{0, 1}, new double[]{1})
                .withTrainingData(new double[]{1, 0}, new double[]{1})
                .withTrainingData(new double[]{1, 1}, new double[]{0})
                .build();
         network.educate(plan);

        System.out.println("Evaluation for (1,1):" + network.evaluate(Arrays.asList(1D, 1D)));
        System.out.println("Evaluation for (0,0):" + network.evaluate(Arrays.asList(0D, 0D)));
        System.out.println("Evaluation for (1,0):" + network.evaluate(Arrays.asList(1D, 0D)));
        System.out.println("Evaluation for (0,1):" + network.evaluate(Arrays.asList(0D, 1D)));
    }
}
