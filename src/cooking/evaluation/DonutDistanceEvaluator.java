package cooking.evaluation;

import java.util.List;

import cooking.database.DbManager;
import cooking.recipe.Recipe;
import cooking.recipe.stats.RecipeSetStats;

public class DonutDistanceEvaluator implements IEvaluator {

	/**
	 * @param stats
	 * @param useSingleLink - if true, uses single link distance, if false, uses average link
	 */
	public DonutDistanceEvaluator(RecipeSetStats stats, boolean useSingleLink) {
	
		this.stats = stats;
		this.useSingleLink = useSingleLink;
	}
	
	boolean useSingleLink;
	RecipeSetStats stats;
	
	public final int USE_ALL_RECIPES_SAMPLE_SIZE = -1;
	public final int DEFAULT_SAMPLE_SIZE = 200;
	private int maxSampleSize = DEFAULT_SAMPLE_SIZE;
	private final String BASE_EVAL_STRING = "This recipe has a donut distance score of ";
	
	@Override
	public Evaluation evaluateRecipe(Recipe recipe) {
		double min = Double.MAX_VALUE;
		double sum = 0;
		
		// TODO: we could optimize further by only getting the random list once
		
		List<Recipe> sample;
		if (getMaxSampleSize() == USE_ALL_RECIPES_SAMPLE_SIZE) {
			sample = DbManager.getSingleton().getAllRecipes();
		}
		else {
			sample = DbManager.getSingleton().getRandomSampleOfRecipes(getMaxSampleSize());
		}
		
		for (Recipe other : sample) {
			double dist = recipe.calculateInterpolatedDistanceFrom(other);
		
			if (useSingleLink) {
				if (dist < min) {
					min = dist;
				}
			}
			else {
				sum += dist;
			}
		}
		
		double distanceValue;
		
		if (useSingleLink) {
			// single link
			distanceValue = min;
		}
		else {
			// average link
			distanceValue = sum / DbManager.getSingleton().getAllRecipes().size();
		}

		double zScore = (distanceValue - stats.getMeanDistance()) / stats.getStandardDeviation();
		double evaluation = produceEvaluation(zScore);
		//evaluation = 0;
		return new Evaluation(evaluation, BASE_EVAL_STRING);
	}
	
	/**
	 * Fits a zScore to the range of 0-1, with a zScore of 0 getting a perfect evaluation (ie, 1),
	 * and one that is a long way off getting close to 0 
	 * 
	 * @param zScore
	 * @return
	 */
	private double produceEvaluation(double zScore) {
		return Math.exp(-1 * Math.pow(zScore, 2) / 2);
	}
	
	public void setMaxSampleSize(int maxSampleSize) {
		this.maxSampleSize = maxSampleSize;
	}

	public int getMaxSampleSize() {
		return maxSampleSize;
	}	
	
}
