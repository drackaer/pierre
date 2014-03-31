package cooking.generation.evolutionary.mutation;

import java.util.List;
import java.util.Random;

import cooking.database.DbManager;
import cooking.generation.evolutionary.MutationType;
import cooking.recipe.Ingredient;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;

public class StatsBasedBasicMutator implements IMutationOperator {

	public Random random = new Random();
	
	private SimilarIngredientSwapper similarIngredientSwapper = new SimilarIngredientSwapper();
	private GaussianQuantityMutator gaussianQuantityMutator = new GaussianQuantityMutator();
	
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
		
		//System.out.println(recipe.toLongString());
		
		Recipe newRecipe = mutate(recipe, ingredientToMutate, typeOfMutation);
		
		//System.out.println(newRecipe.toLongString());
		
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

		MeasuredIngredient originalMeasuredIngredient = newRecipe.getIngredients().get(ingredientIndex);
		List<Ingredient> allIngredients = DbManager.getSingleton().getAllIngredients();
		
		switch(type) {
		case QUANTITY:
			double amount = gaussianQuantityMutator.getRandomOuncesAccordingToGaussian(originalMeasuredIngredient.getIngredient());
			originalMeasuredIngredient.setQuantity(amount);
			break;
			
		case INGREDIENT:
			Ingredient newIngredient = similarIngredientSwapper.getSimilarIngredient(originalMeasuredIngredient.getIngredient());
			originalMeasuredIngredient.setIngredient(newIngredient);
			break;
			
		case ADDITION:
			Ingredient ing = allIngredients.get(random.nextInt(allIngredients.size()));
			double ounces = gaussianQuantityMutator.getRandomOuncesAccordingToGaussian(ing);
			
			MeasuredIngredient mi = new MeasuredIngredient();
			
			mi.setIngredient(ing);
			mi.setAmountInOunces(ounces);
			
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
