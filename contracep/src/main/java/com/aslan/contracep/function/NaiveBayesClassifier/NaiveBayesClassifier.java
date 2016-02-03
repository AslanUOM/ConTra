package com.aslan.contracep.function.NaiveBayesClassifier;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NaiveBayesClassifier {

	public NaiveBayesClassifier() {

	}

	public ArrayList<double[]> loadCsv(String filepath) {

		String csvFile = filepath;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<double[]> dataset = new ArrayList<double[]>();

		try {

			// Map<String, String> maps = new HashMap<String, String>();

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] datarow = line.split(cvsSplitBy);
				double[] datarowInt = new double[datarow.length];

				// convert to int
				for (int i = 0; i < datarow.length; i++) {
					datarowInt[i] = Double.parseDouble(datarow[i]);
				}

				dataset.add(datarowInt);
				// maps.put(country[4], country[5]);

			}

			// loop map
			// for (Map.Entry<String, String> entry : maps.entrySet()) {
			//
			// System.out.println("Country [code= " + entry.getKey() +
			// " , name="
			// + entry.getValue() + "]");
			//
			// }

//			for (int i = 0; i < dataset.size(); i++) {
//
//				double[] datarow = dataset.get(i);
//				for (int j = 0; j < datarow.length; j++) {
//					System.out.print(datarow[j] + "  ");
//				}
//
//				System.out.println();
//
//			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//System.out.println("Done");
		return dataset;
	}

	public Map<Double, ArrayList<double[]>> separateByClass(
			ArrayList<double[]> dataset) {
		// ArrayList<Double> separated
		Map<Double, ArrayList<double[]>> separated = new HashMap<Double, ArrayList<double[]>>();

		ArrayList<double[]> temp0 = new ArrayList<double[]>();
		ArrayList<double[]> temp1 = new ArrayList<double[]>();

		for (int i = 0; i < dataset.size(); i++) {

			double[] vector = dataset.get(i);
			if (vector[vector.length - 1] == 0.0) {
				temp0.add(vector);
			} else if (vector[vector.length - 1] == 1.0) {
				temp1.add(vector);
			}
		}

		separated.put(0.0, temp0);
		separated.put(1.0, temp1);

		// double[] ex1 = {2.0,3.0,4.5};
		// double[] ex2 = {5.0,6.0,7.5};
		// double[] ex3 = {8.0,9.0,10.5};
		// double[] ex4 = {11.0,12.0,13.5};
		//
		// separated.put(0.0, ex1);
		// separated.put(1.0, ex2);
		// separated.put(0.0, ex3);
		// separated.put(1.0, ex4);
		//
		// //loop map
		// for (Map.Entry<Double, double[]> entry : separated.entrySet()) {
		//
		// System.out.println("Row [class= " + entry.getKey() + " , value="
		// + entry.getValue()[0] + "]");
		//
		// }

		// for(int i=0;i<dataset.size();i++){
		// double[] vector = dataset.get(i);
		// if(vector[vector.length-1])
		// }

		return separated;
	}

	public double mean(double[] dataInstance) {
		double sum = 0;
		double mean = 0;
		int size = dataInstance.length;
		for (int i = 0; i < dataInstance.length; i++) {
			sum += dataInstance[i];
		}

		mean = sum / (double) size;
		return mean;
	}

	public double stdev(double[] dataInstance) {
		double avg = mean(dataInstance);
		double sum = 0;
		int size = dataInstance.length;
		for (int i = 0; i < dataInstance.length; i++) {
			sum += Math.pow(dataInstance[i] - avg, 2);
		}
		double variance = sum / ((double) size - 1);
		return Math.sqrt(variance);

	}

	public ArrayList<double[]> summarize(ArrayList<double[]> dataset) {

		ArrayList<double[]> summaries = new ArrayList<double[]>();

		int sizeOfInstance = dataset.get(0).length;

		for (int j = 0; j < sizeOfInstance - 1; j++) {

			double[] instance = new double[dataset.size()];

			for (int i = 0; i < dataset.size(); i++) {
				instance[i] = dataset.get(i)[j];
			}

			double mean = mean(instance);
			double stdev = stdev(instance);

			double[] summaryOfInstance = new double[2];
			summaryOfInstance[0] = mean;
			summaryOfInstance[1] = stdev;

			summaries.add(summaryOfInstance);
		}

		return summaries;
	}

	public Map<Double, ArrayList<double[]>> summarizeByClass(
			ArrayList<double[]> dataset) {
		Map<Double, ArrayList<double[]>> separated = new HashMap<Double, ArrayList<double[]>>();
		separated = separateByClass(dataset);

		ArrayList<double[]> temp0 = new ArrayList<double[]>();
		ArrayList<double[]> temp1 = new ArrayList<double[]>();

		temp0 = separated.get(0.0);
		temp0 = summarize(temp0);

		temp1 = separated.get(1.0);
		temp1 = summarize(temp1);

		Map<Double, ArrayList<double[]>> summaries = new HashMap<Double, ArrayList<double[]>>();
		summaries.put(0.0, temp0);
		summaries.put(1.0, temp1);

		return summaries;
	}

	public double calculateProbability(double x, double mean, double stdev) {
		double exponent = Math.exp(-(Math.pow(x - mean, 2) / (2 * Math.pow(
				stdev, 2))));
		double prob = (1 / (Math.sqrt(2 * Math.PI) * stdev)) * exponent;
		return prob;
	}

	public double[] calculateClassProbabilities(
			Map<Double, ArrayList<double[]>> summaries, double[] inputVector) {
		// Map<Double, ArrayList<double[]>> probabilities = new HashMap <Double,
		// ArrayList<double[]>>();

		double probabilities[] = new double[2];
		probabilities[0] = 1;
		probabilities[1] = 1;

		ArrayList<double[]> class0_summary = summaries.get(0.0);

		for (int i = 0; i < class0_summary.size(); i++) {

			double mean = class0_summary.get(i)[0];
			double stdev = class0_summary.get(i)[1];
			double x = inputVector[i];
			probabilities[0] *= calculateProbability(x, mean, stdev);

		}

		ArrayList<double[]> class1_summary = summaries.get(1.0);

		for (int i = 0; i < class1_summary.size(); i++) {
			// to have to classes probabilities
			// probabilities[1] = 1;

			double mean = class1_summary.get(i)[0];
			double stdev = class1_summary.get(i)[1];
			double x = inputVector[i];
			probabilities[1] *= calculateProbability(x, mean, stdev);

		}

		return probabilities;
	}

	public String predict(Map<Double, ArrayList<double[]>> summaries,
			double[] inputVector) {
		double probabilities[] = new double[2];
		probabilities = calculateClassProbabilities(summaries, inputVector);

		String bestLabel = null;
		double bestProb = -1;

		for (int i = 0; i < probabilities.length; i++) {
			if (bestLabel == null || probabilities[i] > bestProb) {
				bestProb = probabilities[i];
				bestLabel = Integer.toString(i);
			}
		}

		return bestLabel;

	}

	public ArrayList<String> getPredictions(
			Map<Double, ArrayList<double[]>> summaries,
			ArrayList<double[]> testSet) {
		ArrayList<String> predictions = new ArrayList<String>();
		for (int i = 0; i < testSet.size(); i++) {
			String result = predict(summaries, testSet.get(i));
			predictions.add(result);
		}

		return predictions;
	}

}
