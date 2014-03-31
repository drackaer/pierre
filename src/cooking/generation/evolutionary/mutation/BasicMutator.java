package cooking.generation.evolutionary.mutation;

import java.util.List;
import java.util.Random;

import cooking.database.DbManager;
import cooking.generation.evolutionary.MutationType;
import cooking.recipe.Ingredient;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;

public class BasicMutator implements IMutationOperator {

	private double quantityMutationAmount = .2;
	private int maxAdditionAmountOunces = 10;
	
	public Random random = new Random();
	
	public void setQuantityMutationAmount(double quantityMutationAmount) {
		this.quantityMutationAmount = quantityMutationAmount;
	}

	public double getQuantityMutationAmount() {
		return quantityMutationAmount;
	}

	public void setMaxAdditionAmountOunces(int maxAdditionAmountOunce) {
		this.maxAdditionAmountOunces = maxAdditionAmountOunce;
	}

	public int getMaxAdditionAmountOunces() {
		return maxAdditionAmountOunces;
	}
	
	
	@Override
	public Recipe performMutation(Recipe original) throws Exception {
		return performRandomMutation(original);
	}

	/**
	 * Picks a random ingredient and performs a random type of mutation on it
	 * 
	 * @param recipe
	 * @return
	 * @throws Exception
	 */
	private Recipe performRandomMutation(Recipe recipe) throws Exception {
		int ingredientToMutate = random.nextInt(recipe.getIngredients().size());
		MutationType typeOfMutation = MutationType.values()[random.nextInt(MutationType.values().length)];
		
		Recipe newRecipe = mutate(recipe, ingredientToMutate, typeOfMutation);
		
		return newRecipe;
	}
	
	/**
	 * Performs a simple mutation of the type specified.
	 * 
	 * @param original
	 * @param ingredientIndex
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public Recipe mutate(Recipe original, int ingredientIndex, MutationType type) throws Exception {
		Recipe newRecipe = original.clone();

		MeasuredIngredient ingredient = newRecipe.getIngredients().get(ingredientIndex);
		List<Ingredient> allIngredients = DbManager.getSingleton().getAllIngredients();
		
		switch(type) {
		case QUANTITY:
			double mutationFactor = 1 - getQuantityMutationAmount() + (random.nextDouble() * getQuantityMutationAmount() * 2);
			double quantity = ingredient.getQuantity() * mutationFactor;
			quantity = ((double)Math.round(quantity * 100)) / 100;
			ingredient.setQuantity(quantity);
			break;
			
		case INGREDIENT:
			ingredient.setIngredient(allIngredients.get(random.nextInt(allIngredients.size())));
			break;
			
		case ADDITION:
			int ounces = random.nextInt(getMaxAdditionAmountOunces()) + 1;
			Ingredient ing = allIngredients.get(random.nextInt(allIngredients.size()));
			
			String newIngString = ounces + " ounces " + ing.getName();
			
			MeasuredIngredient mi = new MeasuredIngredient(newIngString);
			newRecipe.getIngredients().add(ingredientIndex, mi);
			break;
			
		case DELETION:
			newRecipe.getIngredients().remove(ingredientIndex);
			break;
			
		default:
			throw new Exception("Mutation Type not implemented");
		}
		
		return newRecipe;
	}
	
}
