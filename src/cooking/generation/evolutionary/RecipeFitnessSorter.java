package cooking.generation.evolutionary;

import java.util.Comparator;

import cooking.evaluation.Evaluation;
import cooking.evaluation.IEvaluator;
import cooking.recipe.Recipe;

public class RecipeFitnessSorter implements Comparator<Recipe> {

	public RecipeFitnessSorter(IEvaluator evaluator) {
		setEvaluator(evaluator);
	}
	
	private IEvaluator evaluator;
	
	@Override
	public int compare(Recipe first, Recipe second) {

		Evaluation evalFirst;
		Evaluation evalSecond;
		
		if (first.hasTemporaryEvaluation()) {
			evalFirst = first.getTemporaryEvaluation();
		}
		else {
			evalFirst = getEvaluator().evaluateRecipe(first);
			first.setTemporaryEvaluation(evalFirst);
		}
		
		if (second.hasTemporaryEvaluation()) {
			evalSecond = second.getTemporaryEvaluation();
		}
		else {
			evalSecond = getEvaluator().evaluateRecipe(second);
			second.setTemporaryEvaluation(evalSecond);
		}
		
		// flip the sign on the compare so we get descending order
		return -1 * Double.compare(evalFirst.getNumericalEvaluation(), evalSecond.getNumericalEvaluation());
	}

	public void setEvaluator(IEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public IEvaluator getEvaluator() {
		return evaluator;
	}

}
