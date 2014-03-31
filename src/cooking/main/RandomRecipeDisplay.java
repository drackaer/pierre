package cooking.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cooking.database.InspiringSetType;
import cooking.database.DbManager;
import cooking.evaluation.Evaluation;
import cooking.evaluation.EvaluatorType;
import cooking.evaluation.IEvaluator;
import cooking.evaluation.InterpolatedEvaluator;
import cooking.recipe.Ingredient;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;
import cooking.recipe.stats.Aggregator;
import cooking.recipe.stats.StatsSummary;

public class RandomRecipeDisplay {
	
	public static void main (String[] args) throws Exception {
		int numRecipes = 10;
		int maxIngredients = 15;
		DbManager inst = DbManager.getSingleton();
		inst.loadData(InspiringSetType.FULL, true);
		Map<Ingredient, StatsSummary> map = Aggregator.getIngredientStatsMap();

		IEvaluator eval = new InterpolatedEvaluator(EvaluatorType.CHILI);
		
		List<Recipe> newRecipes = new ArrayList<Recipe>();
		List<Ingredient> ingredients = inst.getAllIngredients();
		
		for (int i = 0; i < numRecipes; i++) {
			Random rand = new Random();
			int numIngredients = rand.nextInt(maxIngredients) + 1;
			
			List<MeasuredIngredient> newIngredients = new ArrayList<MeasuredIngredient>();
			
			for (int j = 0; j < numIngredients; j++) {
				Ingredient newIng = ingredients.get(rand.nextInt(ingredients.size()));
				double quantity = 0;
				
				StatsSummary stats = map.get(newIng);
				while (stats == null) {
					newIng = ingredients.get(rand.nextInt(ingredients.size()));
					stats = map.get(newIng);
				}
				
				double mean = stats.getMean();
				double variance = stats.getVariance();
				double max = stats.getMax();
				double min = stats.getMin();
				
				while (quantity < min || quantity > max || quantity < 0.01) {
					quantity = mean + rand.nextGaussian() * variance;
				}
				
				MeasuredIngredient newIngredient = new MeasuredIngredient(newIng, quantity);
				
				newIngredients.add(newIngredient);
			}
			Recipe newRecipe = new Recipe(newIngredients);
			
			newRecipes.add(newRecipe.getScaledVersion(DbManager.getScaleSize()));
		}
		
		for (Recipe recipe : newRecipes) {
			Evaluation rating = eval.evaluateRecipe(recipe);
			recipe.setTemporaryEvaluation(rating);
			
			System.out.println(recipe.toLongString());
		}
	}
}
