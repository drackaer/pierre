package cooking.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cooking.database.DbManager;
import cooking.database.ItemNotFoundException;
import cooking.evaluation.Evaluation;
import cooking.evaluation.IEvaluator;
import cooking.recipe.Ingredient;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;
import cooking.recipe.stats.Aggregator;
import cooking.recipe.stats.StatsSummary;

public class GreedyGenerator implements IGenerator {

	private IEvaluator evaluator;
	
	@Override
	public Recipe generateRecipe() {
		DbManager inst = DbManager.getSingleton();
		List<Recipe> recipes = inst.getAllRecipes();
		
		// double to avoid casting issues
		//double sum = 0;
		//for(Recipe r : recipes) {
		//	sum += r.getIngredients().size();
		//}
		
		Random rand = new Random();
//		double averageRecipeSize = sum / recipes.size();
		double averageRecipeSize = 11.88345626975764;
		
		// This recipe will have a size of the average recipe size within a range of + or - 2
		int currentRecipeSize = (int) Math.floor(averageRecipeSize + (rand.nextInt(5) - 2));
		
		
		List<Ingredient> ingredients = inst.getAllIngredients();
		List<MeasuredIngredient> newIngredientList = new ArrayList<MeasuredIngredient>();
		
		for (int i = 0; i < currentRecipeSize; i++) {
			// Change to choose greedily
			MeasuredIngredient newIng = chooseNextIngredient(newIngredientList);
			newIngredientList.add(newIng);
		}
		
		Recipe newRecipe = new Recipe(newIngredientList);
		try {
			newRecipe.combineSameIngredients();
			newRecipe = newRecipe.getScaledVersion(DbManager.getScaleSize());
			newRecipe.setTemporaryEvaluation(evaluator.evaluateRecipe(newRecipe));
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
		}
		
		return newRecipe;
	}

	private MeasuredIngredient chooseNextIngredient(List<MeasuredIngredient> currentIngredients) {
		MeasuredIngredient bestIngredient = null;
		double bestEvaluation = Double.MIN_VALUE;
		Random rand = new Random();
		
		for (Ingredient ing : DbManager.getSingleton().getAllIngredients())
		{
			StatsSummary stats = Aggregator.getSmoothedStatsObject(ing);
			
			double quantity = 0;
			double mean = stats.getMean();
			double variance = stats.getVariance();
			double max = stats.getMax();
			double min = stats.getMin();
			
			while (quantity < min || quantity > max || quantity < 0.01) {
				quantity = mean + rand.nextGaussian() * variance;
			}
			
			try {
				MeasuredIngredient newIng = new MeasuredIngredient(ing, quantity);
				List<MeasuredIngredient> newIngredients = new ArrayList<MeasuredIngredient>();
				newIngredients.addAll(currentIngredients);
				newIngredients.add(newIng);
				Recipe recipe = new Recipe(newIngredients);
				Evaluation eval = evaluator.evaluateRecipe(recipe.getScaledVersion(DbManager.getScaleSize()));
				
				if (eval.getNumericalEvaluation() > bestEvaluation) {
					bestEvaluation = eval.getNumericalEvaluation();
					bestIngredient = newIng;
				}
			} catch (ItemNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return bestIngredient;
	}

	public void setEvaluator(IEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public IEvaluator getEvaluator() {
		return evaluator;
	}

}
