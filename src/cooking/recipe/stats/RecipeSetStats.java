package cooking.recipe.stats;

import java.util.List;

import org.apache.log4j.Logger;

import cooking.recipe.Recipe;

public class RecipeSetStats {

	public RecipeSetStats(List<Recipe> list) {
		calculateStats(list);
	}
	
	public RecipeSetStats(double min, double max, double mean, double variance) {
		stats = new StatsSummary();
		stats.setMax(max);
		stats.setMean(mean);
		stats.setMin(min);
		stats.setVariance(variance);
	}
	
	private Logger logger = Logger.getLogger(this.getClass());
	private StatsSummary stats;
	
	public double getMinDistance() {
		return stats.getMin();
	}
	public double getMaxDistance() {
		return stats.getMax();
	}
	public double getMeanDistance() {
		return stats.getMean();
	}
	public double getVariance() {
		return stats.getVariance();
	}
	public double getStandardDeviation() {
		return stats.getStandardDeviation();
	}
	
	private void calculateStats(List<Recipe> list) {
		logger.info("Calculating stats for a recipe list with " + list.size() + " recipes.");

		stats = new StatsSummary();

		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = i + 1; j < list.size(); j++) {
				Recipe first = list.get(i);
				Recipe second = list.get(j);
				
				double distance = first.calculateInterpolatedDistanceFrom(second);
			
				stats.UpdateWithNewValue(distance);
			}
		}
		
		logger.info("Done calculating stats for recipe list.");
	}
}
