package cooking.evaluation;

import cooking.recipe.Recipe;

public class InterpolatedNeuralNetworkEvaluator implements IEvaluator {

	private SubGroupNeuralNetworkEvaluator subGroupEvaluator;
	private SuperGroupNeuralNetworkEvaluator superGroupEvaluator;
	private double weight;
	
	public InterpolatedNeuralNetworkEvaluator(EvaluatorType type) throws Exception{
		subGroupEvaluator = NeuralNetworkEvaluatorGenerator.getSingleton().getSubGroupEvaluator(type);
		superGroupEvaluator = NeuralNetworkEvaluatorGenerator.getSingleton().getSuperGroupEvaluator(type);
		weight = 0.6;
	}

	/* (non-Javadoc)
	 * @see cooking.evaluation.IEvaluator#evaluateRecipe(cooking.recipe.Recipe)
	 */
	@Override
	public Evaluation evaluateRecipe(Recipe recipe) {
		Evaluation subGroupEvaluation = subGroupEvaluator.evaluateRecipe(recipe);
		Evaluation superGroupEvaluation = superGroupEvaluator.evaluateRecipe(recipe);
		
		String evalString = subGroupEvaluation.getStringEvaluation() + "\n";
		evalString += superGroupEvaluation.getStringEvaluation() + "\n";
		evalString += "With an overall rating of ";
		
		double evaluation = (subGroupEvaluation.getNumericalEvaluation() * weight) + (superGroupEvaluation.getNumericalEvaluation() * (1 - weight));
		return new Evaluation(evaluation,evalString);
	}
}
