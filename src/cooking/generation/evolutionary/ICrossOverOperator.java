package cooking.generation.evolutionary;

import cooking.recipe.Recipe;

public interface ICrossOverOperator {

	/**
	 * Returns a new recipe that is somehow a combination of the two given recipes.
	 * @param first
	 * @param second
	 * @return
	 */
	public Recipe crossOver(Recipe first, Recipe second);
}
