package cooking.generation.evolutionary.mutation;

import java.util.Random;

import org.apache.log4j.Logger;

import cooking.recipe.Ingredient;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;
import cooking.recipe.stats.Aggregator;
import cooking.recipe.stats.StatsSummary;

public class GaussianQuantityMutator implements IMutationOperator {
	private double probabilityOfMutation = .2;
	
	public Random random = new Random();
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public Recipe performMutation(Recipe original) throws Exception {
		
		//System.out.println(original.toLongString());
	
		Recipe newRecipe = original.clone();

		for (MeasuredIngredient mi : newRecipe.getIngredients()) {
			
			if (random.nextDouble() < probabilityOfMutation) {
				double ounces = getRandomOuncesAccordingToGaussian(mi.getIngredient());
				
				logger.info("Mutated " + mi.getIngredient().getName() + " from " + mi.getAmountInOunces() + " to " + ounces + " ounces");
				
				mi.setAmountInOunces(ounces);
			}
		}
		
		//System.out.println(newRecipe.toLongString());
		
		return newRecipe;
	}
	
	public double getRandomOuncesAccordingToGaussian(Ingredient ingredient) {
		
		StatsSummary stats = Aggregator.getSmoothedStatsObject(ingredient);
		
		double gaussian = random.nextGaussian();
		double amount = gaussian * stats.getStandardDeviation() + stats.getMean();

		return amount;
	}

	public void setProbabilityOfMutation(double probabilityOfMutation) {
		this.probabilityOfMutation = probabilityOfMutation;
	}

	public double getProbabilityOfMutation() {
		return probabilityOfMutation;
	}
}
