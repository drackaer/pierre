package cooking.evaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cooking.database.DbManager;
import cooking.database.ItemNotFoundException;
import cooking.neuralnetwork.BackpropagationRegression;
import cooking.recipe.Recipe;

public class SubGroupNeuralNetworkEvaluator implements IEvaluator, Serializable {

	private static final long serialVersionUID = 1L;
	private List<Double> max, min;
	private BackpropagationRegression learner;
	
	public SubGroupNeuralNetworkEvaluator() {
		ArrayList<ArrayList<Double>> data = DbManager.getSingleton().getSubGroupLearnerData();
		findMaxAndMin(data);
		data = normalizeAll(data);
		ArrayList<ArrayList<Double>> validationData = extractDataSet(data, 0.2);
		ArrayList<ArrayList<Double>> testData = extractDataSet(data, 0.2);
		BackpropagationRegression learner = new BackpropagationRegression(data, testData, validationData, 16, 0.01, 50);
		try {
			learner.train();
			this.learner = learner;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<ArrayList<Double>> normalizeAll(ArrayList<ArrayList<Double>> data) {
		ArrayList<ArrayList<Double>> tempData = data;
		for (int i = 0; i < tempData.size(); i++) {
			for (int j = 0; j < tempData.get(i).size(); j++) {
				if (max.get(j) - min.get(j) == 0) {
					tempData.get(i).set(j, 0.0);
				} else {
					double newValue = (tempData.get(i).get(j) - min.get(j))	/ (max.get(j) - min.get(j));
					tempData.get(i).set(j, newValue);
				}
			}
		}
		return tempData;
	}
	
	/**
	 * Assuming amount is a double between 0 and 1.
	 */
	private ArrayList<ArrayList<Double>> extractDataSet(ArrayList<ArrayList<Double>> data, double amount) {
		ArrayList<ArrayList<Double>> newSet = new ArrayList<ArrayList<Double>>();
		Random gen = new Random();
		double size = data.size();
		while(newSet.size()/size < amount) {
			int rand = gen.nextInt(data.size());
			newSet.add(data.remove(rand));
		}
		return newSet;
	}
	
	private void findMaxAndMin(ArrayList<ArrayList<Double>> data) {
		ArrayList<Double> max = new ArrayList<Double>();
		ArrayList<Double> min = new ArrayList<Double>();
		for (ArrayList<Double> instance : data) {
			// Find Max and min for each attribute of the data
			for (int i = 0; i < data.size(); i++) {
				for (int j = 0; j < data.get(i).size(); j++) {
					// initialize the max and mins
					if (i == 0) {
						max.add(0.0);
						min.add(Double.MAX_VALUE);
					}
					if (max.get(j) < data.get(i).get(j)) {
						max.set(j, data.get(i).get(j));
					}
					if (min.get(j) > data.get(i).get(j)) {
						min.set(j, data.get(i).get(j));
					}
				}
			}
		}
		this.max = max;
		this.min = min;
	}

	@Override
	public Evaluation evaluateRecipe(Recipe recipe) {
		DbManager manager = DbManager.getSingleton();
		double[] attributeVector = new double[0];
		ArrayList<Double> instance = new ArrayList<Double>();
		try {
			attributeVector = manager.convertRecipeToSubGroupFormat(recipe);
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
		}
		
		for (double d : attributeVector) {
			instance.add(d);
		}
		
		instance = normalizeAttributeVector(instance);
		double eval = 0;
		try {
			eval = evaluateInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Evaluation(eval,"This recipe has a sub group star rating of ");
	}
	
	private ArrayList<Double> normalizeAttributeVector(ArrayList<Double> attributeVector) {
		// Remove spot for rating since there isn't one on new instances
		ArrayList<Double> normalized = new ArrayList<Double>();
		
		for (int i = 0; i < attributeVector.size() - 1; i++) {
			if (max.get(i) - min.get(i) == 0) {
				normalized.add(0.0);
			} else {
				normalized.add((attributeVector.get(i) - min.get(i)) / (max.get(i) - min.get(i)));
			}
		}
		return normalized;
	}

	private double evaluateInstance(ArrayList<Double> instance) throws Exception {
		return learner.evaluateInstance(instance);
	}
}
