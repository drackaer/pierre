package cooking.generation.evolutionary.mutation;

import java.util.List;
import java.util.Random;

import cooking.database.IngredientGroup;
import cooking.recipe.Ingredient;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;

public class SimilarIngredientSwapper implements IMutationOperator {

	private double probabilityOfMutation = .2;
	private int numberOfTiersToGoUp = 1;
	
	public Random random = new Random();
	
	@Override
	public Recipe performMutation(Recipe original) throws Exception {
		
		//System.out.println(original.toLongString());
	
		Recipe newRecipe = original.clone();
		
		for (int i = 0; i < newRecipe.getIngredients().size(); i++) {
			if (random.nextDouble() < probabilityOfMutation) {
				List<MeasuredIngredient> list = newRecipe.getIngredients();
				
				MeasuredIngredient oldMi = list.get(i);
				MeasuredIngredient newMi = swapForSimilarIngredient(oldMi);
				
				list.remove(i);
				list.add(i, newMi);
			}
		}
		
		//System.out.println(newRecipe.toLongString());
		
		return newRecipe;
	}
	
	public MeasuredIngredient swapForSimilarIngredient(MeasuredIngredient original) {
		Ingredient ingredient = getSimilarIngredient(original.getIngredient());

		MeasuredIngredient newMeasuredIngredient = original.clone();
		newMeasuredIngredient.setIngredient(ingredient);
		
		return newMeasuredIngredient;
	}
	
	public Ingredient getSimilarIngredient(Ingredient original) {
		IngredientGroup group  = original.getParentGroup();
		
		for (int i = 1; i < getNumberOfTiersToGoUp(); i++) {
			if (group.hasParents()) {
				group = group.getParentGroup();
			}
			else {
				break;
			}
		}

		List<Ingredient> allIngredients = group.getAllIngredients();

		Ingredient newIngredient = allIngredients.get(random.nextInt(allIngredients.size()));
		
		return newIngredient;
	}

	public void setProbabilityOfMutation(double probabilityOfMutation) {
		this.probabilityOfMutation = probabilityOfMutation;
	}

	public double getProbabilityOfMutation() {
		return probabilityOfMutation;
	}

	public void setNumberOfTiersToGoUp(int numberOfTiersToGoUp) {
		this.numberOfTiersToGoUp = numberOfTiersToGoUp;
	}

	public int getNumberOfTiersToGoUp() {
		return numberOfTiersToGoUp;
	}

}
