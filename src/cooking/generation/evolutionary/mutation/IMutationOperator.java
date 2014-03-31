package cooking.generation.evolutionary.mutation;

import cooking.recipe.Recipe;

public interface IMutationOperator {

	/**
	 * Clones the original recipe, performs some type of mutation,
	 * and returns a new mutated version.
	 * 
	 * @param original
	 * @return
	 */
	public Recipe performMutation(Recipe original) throws Exception;
}
