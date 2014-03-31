package cooking.evaluation;

import org.apache.log4j.Logger;

import cooking.database.DbManager;
import cooking.recipe.Recipe;

public class CentroidEvaluator implements IEvaluator {

	private final String BASE_EVAL_STRING = "This recipe has a centroid distance of ";
	
	private Logger logger = Logger.getLogger(this.getClass()); 
	
	@Override
	public Evaluation evaluateRecipe(Recipe recipe) {
		double dist = recipe.getDistanceFromCentroid();

		if (dist < 0) {
			logger.error("Error: expected distance to be between 0 and 1, and received: " + dist + ".  Using distance of 0 instead.");
			dist = 0;
		}
		else if (dist > 1) {
			logger.error("Error: expected distance to be between 0 and 1, and received: " + dist + ".  Using distance of 1 instead.");
			dist = 1;
		}
		
		double value = 1 - dist;
		
		
		Evaluation eval = new Evaluation(value, "Centroid distance of: ");
		
		return eval; 
		
	}
}
