package cooking.generation.evolutionary.mutation;

import cooking.generation.evolutionary.ICrossOverOperator;
import cooking.recipe.Recipe;

public class KeepFirstParent implements ICrossOverOperator {

	@Override
	public Recipe crossOver(Recipe first, Recipe second) {
		return first;
	}

}
