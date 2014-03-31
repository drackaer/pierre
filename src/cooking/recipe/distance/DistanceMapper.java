package cooking.recipe.distance;

import java.util.List;

public class DistanceMapper {

	/**
	 * @param sortedAndNormalizedRecipeDistances
	 * 
	 * It is assumed that the list is ordered by distance from smallest to greatest
	 */
	public static void printDistanceGraph(
			List<RecipeDistanceObject> sortedAndNormalizedRecipeDistances) {
		StringBuffer graph = new StringBuffer("--------------------------------------------------\n");
		//String addLine = "                                                  \n";
		
		
		int replaceIndex;
		String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		for(int i = 0; i < sortedAndNormalizedRecipeDistances.size();i++){
			RecipeDistanceObject distanceObject = sortedAndNormalizedRecipeDistances.get(i);
			replaceIndex = (int) (distanceObject.getPercentDistantOfGroupMax()*50);
			if(graph.charAt(replaceIndex) != '-')
				graph.replace(replaceIndex, replaceIndex + 1, "*");
			graph.replace(replaceIndex, replaceIndex + 1, alphabet[i]);
			
		}
		System.out.println(graph);
		System.out.println("* - indicates multiple recipes at this location");
		
		System.out.println("\nKEY:\n");
		
		for(int i = 0; i < sortedAndNormalizedRecipeDistances.size();i++){
			RecipeDistanceObject distanceObject = sortedAndNormalizedRecipeDistances.get(i);
			System.out.printf(alphabet[i] + " - %2.2f%% (%4.2f) - " + distanceObject.getQueryRecipe().getName() + "\n",distanceObject.getPercentDistantOfGroupMax()*100,distanceObject.getDistance());
		}		
		for(int i = 0; i < sortedAndNormalizedRecipeDistances.size();i++){
			RecipeDistanceObject distanceObject = sortedAndNormalizedRecipeDistances.get(i);
			System.out.println(distanceObject.getQueryRecipe().toLongString());
		}
	}

}
