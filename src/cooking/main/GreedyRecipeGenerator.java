package cooking.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cooking.database.InspiringSetType;
import cooking.database.DbManager;
import cooking.evaluation.EvaluatorType;
import cooking.evaluation.IEvaluator;
import cooking.evaluation.InterpolatedEvaluator;
import cooking.evaluation.InterpolatedNeuralNetworkEvaluator;
import cooking.generation.GreedyGenerator;
import cooking.recipe.Recipe;

public class GreedyRecipeGenerator {
	
	public static int numRecipes = 10;
	
	public static void main (String[] args) throws Exception {
		GreedyGenerator gen = new GreedyGenerator();
		IEvaluator evaluator = new InterpolatedNeuralNetworkEvaluator(EvaluatorType.CHILI);
		gen.setEvaluator(evaluator);
		
		DbManager inst = DbManager.getSingleton();
		try {
			inst.loadData(InspiringSetType.FULL, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Recipe> recipes = new ArrayList<Recipe>();
		
		for (int i = 0; i < numRecipes; i++) {
			System.out.println(gen.generateRecipe().toLongString());
		}
	}
}
