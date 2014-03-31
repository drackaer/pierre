package cooking.evaluation;

import cooking.recipe.Recipe;

public class DefaultEvaluator implements IEvaluator{

	
	/**
	 * Evaluates a given recipe giving a score from [0-1] where 1 represents the best evaluation possible.
	 * @param recipe
	 * @return
	 */
	@Override
	public Evaluation evaluateRecipe(Recipe recipe) {
		return new Evaluation(0, "");
	}
	
	
}
