package cooking.generation.evolutionary;

import java.util.List;
import java.util.Random;

import cooking.recipe.Recipe;

public class FitnessProportionSelector implements ISelector {

	public Random random = new Random();
	public double[] wheel = null;
	
	@Override
	public Recipe selectNextParent(List<Recipe> population) {
		
		buildWheel(population);
		
		double number = spinWheel();
		Recipe recipe = getItemFromWheel(number, population);
		
		return recipe;
	}
	
	public void buildWheel(List<Recipe> population) {
		wheel = new double[population.size()];
		
		double fitnessSum = 0;
		
		for (Recipe r : population) {
			fitnessSum += r.getTemporaryEvaluation().getNumericalEvaluation();
		}
		
		double sumOfProbabilities = 0;

		for (int i = 0; i < population.size(); i++) {
			Recipe r = population.get(i);
			
			double fitnessProportion = r.getTemporaryEvaluation().getNumericalEvaluation() / fitnessSum;
			sumOfProbabilities = sumOfProbabilities + fitnessProportion;
			
			wheel[i] = sumOfProbabilities;
		}
	}
	
	private double spinWheel() {
		return random.nextDouble();
	}
	
	private Recipe getItemFromWheel(double number, List<Recipe> population) {
		Recipe recipe = null;
		
		// this could be optimized by doing a binary search
		for (int i = 0; i < wheel.length; i++) {
			double wheelValue = wheel[i];
			
			if (number <= wheelValue) {
				recipe = population.get(i);
				break;
			}
		}
		
		return recipe;
	}

}
