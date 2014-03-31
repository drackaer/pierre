package cooking.recipe.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cooking.database.DbManager;
import cooking.database.IngredientGroup;
import cooking.database.ItemNotFoundException;
import cooking.recipe.Ingredient;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;

/**
 * @author sburton
 *
 */
public class Aggregator {

	private static Map<Ingredient, StatsSummary> ingredientStatsMap = null;

	private Logger logger = Logger.getLogger(this.getClass());
	
	public Map<Ingredient, StatsSummary> ComputeStats(List<Recipe> recipes) {
		Map<Ingredient, StatsSummary> statsTable = new HashMap<Ingredient, StatsSummary>();

		for (Recipe recipe : recipes) {

			try {
				// we might want to just do this once up front and save it out
				recipe.combineSameIngredients();
			} catch (ItemNotFoundException e) {
				logger.error("Could not find the ounces ingredient when trying to combine same ingredients");
			}
			
			for (MeasuredIngredient mi : recipe.getIngredients()) {
				
				if (!statsTable.containsKey(mi.getIngredient())) {
					StatsSummary summary = new StatsSummary(mi.getAmountInOunces());
					statsTable.put(mi.getIngredient(), summary);
				}
				else {
					StatsSummary summary = statsTable.get(mi.getIngredient());
					summary.UpdateWithNewValue(mi.getAmountInOunces());
				}
			}
		}
		
		for (StatsSummary summary : statsTable.values()) {
			double prob = ((double) summary.getCount()) / recipes.size();
			summary.setProbabilityOfOccurrence(prob);
		}
		
		return statsTable;
	}
	
	
	/**
	 * Computes the aggregate stats for the specified group.
	 * 
	 * Note: this method is not thread safe
	 * 
	 * @param group
	 * @return
	 */
	public StatsSummary ComputeStats(IngredientGroup group) {
		StatsSummary stats = new StatsSummary();

		for (Ingredient i : group.getAllIngredients()) {
			if (getIngredientStatsMap().containsKey(i)) {
				StatsSummary ingredientStats = getIngredientStatsMap().get(i);
				
				stats.UpdateWithSummary(ingredientStats);
			}
		}
		
		return stats;
	}
	
	/**
	 * Clears the cached map, so it will be recomputed on next request
	 * 
	 * Note: this is not threadsafe
	 * 
	 * @return
	 */
	public static void clearIngredientStatsMap() {
		ingredientStatsMap = null;
	}

	/**
	 * Gets a static list of the ingredients stats map for all ingredients in the DB.  This is computed the first time
	 * and then cache for subsequent calls.
	 * 
	 * Note this is not threadsafe
	 * 
	 * @return
	 */
	public static Map<Ingredient, StatsSummary> getIngredientStatsMap() {
		if (ingredientStatsMap == null) {
			Aggregator agg = new Aggregator();
			ingredientStatsMap = agg.ComputeStats(DbManager.getSingleton().getAllRecipes());
		}
		
		return ingredientStatsMap;
	}
	
	/**
	 * Gets a stats object for the given ingredient.  If no stats (or limited stats) are
	 * available for the ingredient, it will be smoothed by using stats for the sub group
	 * 
	 * Note: this method is not thread safe
	 * 
	 * @param ingredient
	 * @return
	 */
	public static StatsSummary getSmoothedStatsObject(Ingredient ingredient) {
		IngredientGroup group = ingredient.getParentGroup();
		
		StatsSummary groupStats = group.getStatsSummary();
		StatsSummary ingStats = null;
		
		if (getIngredientStatsMap().containsKey(ingredient)) {
			ingStats = getIngredientStatsMap().get(ingredient);
		}
		else {
			ingStats = new StatsSummary();
		}
		
		StatsSummary combinedStats = ingStats.interpolateWith(groupStats);
		
		return combinedStats;
		
	}
	
	/**
	 * Returns true if the Aggregator contains the requested ingredient
	 * @param ing
	 * @return
	 */
	public static boolean containsIngredient(Ingredient ing) {
		return getIngredientStatsMap().containsKey(ing);
	}
	
}
