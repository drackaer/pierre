package cooking.generation;

import cooking.recipe.Recipe;

public interface IGenerator {

	/**
	 * Generates a new recipe...somehow
	 * 
	 * @return
	 */
	public Recipe generateRecipe();
}
