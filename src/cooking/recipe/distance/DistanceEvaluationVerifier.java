package cooking.recipe.distance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cooking.database.InspiringSetType;
import cooking.database.DbManager;
import cooking.recipe.Recipe;

public class DistanceEvaluationVerifier {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DbManager dbmanager = DbManager.getSingleton();
		dbmanager.loadData(InspiringSetType.FULL, true);
		Logger log = Logger.getLogger(DistanceEvaluationVerifier.class);
		
		int n = 5;
		List<Recipe> allRecipes = dbmanager.getAllRecipes();
		int size = allRecipes.size();
		log.info("Randomly selecting " + n + " recipes from " + size + " recipes in database");
		
		List<Recipe> randomRecipes = new ArrayList<Recipe>();
		
		Random rand = new Random();
		for(int i = 0; i < n; i++){
			randomRecipes.add(allRecipes.get(rand.nextInt(size)));
		}
		
		List<RecipeDistanceObject> sortedAndNormalizedRecipeDistances = sortAndNormalizeRecipes(randomRecipes);
		DistanceMapper.printDistanceGraph(sortedAndNormalizedRecipeDistances);
	}

	private static List<RecipeDistanceObject> sortAndNormalizeRecipes(
			List<Recipe> randomRecipes) {
		
		Recipe subject = randomRecipes.get(0);
		Recipe query;
		double distance;
		double maxDistanceSoFar = -1;
		List<RecipeDistanceObject> recipeDistances = new ArrayList<RecipeDistanceObject>();
		recipeDistances.add(new RecipeDistanceObject(subject, subject, 0));
		
		
		for(int i = 1; i < randomRecipes.size(); i++){
			query = randomRecipes.get(i);
			distance = subject.calculateEuclideanDistanceFrom(query);
			if(distance > maxDistanceSoFar){
				maxDistanceSoFar = distance;
			}
			recipeDistances.add(new RecipeDistanceObject(subject, query, distance));
		}
		Collections.sort(recipeDistances);
		
		for(RecipeDistanceObject distanceObject: recipeDistances){
			distanceObject.setPercentDistantOfGroupMax(distanceObject.getDistance()/maxDistanceSoFar);
		}
				
		return recipeDistances;
	}

}
