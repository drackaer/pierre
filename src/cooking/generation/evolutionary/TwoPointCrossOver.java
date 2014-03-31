package cooking.generation.evolutionary;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;

public class TwoPointCrossOver implements ICrossOverOperator {

	public Random random = new Random();
	
	@Override
	public Recipe crossOver(Recipe first, Recipe second) {
		Recipe newRecipe = new Recipe();
		
		//newRecipe.setName("generated");
		
		List<MeasuredIngredient> firstList = first.getIngredients();
		List<MeasuredIngredient> secondList = second.getIngredients();
		
		if (firstList.size() == 0 || secondList.size() == 0) {
			throw new InvalidParameterException("Crossover cannot be performed on an empty recipe.");
		}
		
		List<MeasuredIngredient> newList = new ArrayList<MeasuredIngredient>();
		
		int crossOverPoint1 = random.nextInt(firstList.size());
		int crossOverPoint2 = random.nextInt(secondList.size());
		
		for (int i = 0; i < crossOverPoint1; i++) {
			newList.add(firstList.get(i).clone());
		}
		
		for (int i = crossOverPoint2; i < secondList.size(); i++) {
			newList.add(secondList.get(i).clone());
		}
		
		newRecipe.setIngredients(newList);
		
		return newRecipe;
	}
	
}
