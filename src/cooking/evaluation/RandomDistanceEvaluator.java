package cooking.evaluation;
import java.util.Random;

import cooking.recipe.Recipe;


public class RandomDistanceEvaluator implements IEvaluator {

	Random rand = new Random();
	@Override
	public Evaluation evaluateRecipe(Recipe recipe) {
		return new Evaluation (rand.nextDouble(), "Random Evaluation of: ");
	}

}
