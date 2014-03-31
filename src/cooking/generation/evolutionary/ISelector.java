package cooking.generation.evolutionary;

import java.util.List;

import cooking.recipe.Recipe;

public interface ISelector {
	public Recipe selectNextParent(List<Recipe> population);
}
