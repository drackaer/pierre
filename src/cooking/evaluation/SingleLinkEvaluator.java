package cooking.evaluation;

import cooking.database.DbManager;
import cooking.recipe.Recipe;

public class SingleLinkEvaluator implements IEvaluator {

	@Override
	public Evaluation evaluateRecipe(Recipe recipe) {
		double min = Double.MAX_VALUE;
		
		for (Recipe other : DbManager.getSingleton().getAllRecipes()) {
			double dist = recipe.calculateEuclideanDistanceFrom(other);
			
			if (dist < min) {
				min = dist;
			}
		}
		
		double evaluation = 1 / min;
		evaluation = sigmoid(evaluation);
		return new Evaluation(evaluation,"This recipe has a single link distance of ");
	}
	
	public double sigmoid(double value) {
		double result = 1 / (1 + Math.exp(-1 * value));
		return result;
	}
}
