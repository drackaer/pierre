/**
 * 
 */
package cooking.evaluation;

import java.util.ArrayList;
import java.util.List;

import cooking.database.DbManager;
import cooking.recipe.Recipe;
import cooking.recipe.stats.RecipeSetStats;

/**
 * @author Paul Bodily
 *
 */
public class InterpolatedEvaluator implements IEvaluator {

	private List<IEvaluator> evaluators;
	private List<Double> weights;
	private int evaluatorCount;
	
	public InterpolatedEvaluator(EvaluatorType type){
		evaluators = new ArrayList<IEvaluator>();
		weights = new ArrayList<Double>();
		
		IEvaluator eval;
		try {
			eval = new InterpolatedNeuralNetworkEvaluator(type);
			addEvaluator(eval,.5);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//RecipeSetStats stats = new RecipeSetStats(DbManager.getSingleton().getAllRecipes());
		//RecipeSetStats stats = new RecipeSetStats(0, 57704.38, 213.21, 747128.95);
		//RecipeSetStats stats = new RecipeSetStats(0, 213, 3.8, 24);
		//RecipeSetStats stats = new RecipeSetStats(0, 57704.38, 213.21, 747.95);
		//RecipeSetStats stats = new RecipeSetStats(0, 2888.0978, 159.3151, 3803.1504);
				
		//addEvaluator(new DonutDistanceEvaluator(stats, true),.5);
		
		addEvaluator(new CentroidEvaluator(), .5);
		
		assertWeightsSumToOne();
	}

	private void assertWeightsSumToOne() {
		double totalWeight = 0.0;
		for(Double weight: weights) totalWeight += weight;
		assert(totalWeight == 1.0);
	}
	
	private void addEvaluator(IEvaluator evaluator,
			double weight) {
		evaluators.add(evaluator);
		weights.add(weight);
		evaluatorCount++;
	}

	/* (non-Javadoc)
	 * @see cooking.evaluation.IEvaluator#evaluateRecipe(cooking.recipe.Recipe)
	 */
	@Override
	public Evaluation evaluateRecipe(Recipe recipe) {
		double evaluation = 0.0;
		String evalString = "";
		
		for(int i = 0; i < evaluatorCount; i++){
			Evaluation currentEval = evaluators.get(i).evaluateRecipe(recipe);
			double weightedValue = weights.get(i) * currentEval.getNumericalEvaluation();
			evaluation += weightedValue;
			evalString += currentEval.getStringEvaluation() + "\n";
		}
		
		evalString += "With an overall rating of ";
		//recipe.setTemporaryEvaluation(evaluation, getEvaluationString(recipe));
		return new Evaluation(evaluation,evalString);
	}
}
