package cooking.generation.evolutionary;

import java.util.List;
import java.util.Random;

import cooking.recipe.Recipe;

public class RandomSelector implements ISelector {

	public RandomSelector() {
		random = new Random();
	}
	
	@Override
	public Recipe selectNextParent(List<Recipe> population) {
		Recipe parent = population.get(random.nextInt(population.size()));
		
		return parent;
	}
	
	public Random random;

}
