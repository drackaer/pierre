package cooking.recipe;

import java.util.HashMap;
import java.util.Set;

import cooking.database.IngredientGroup;
import cooking.recipe.stats.StatsSummary;

public class GroupFormatRecipe extends Recipe {
	protected HashMap<IngredientGroup, Double> ingredientGroupAmounts;
	
	public final String DEFAULT_GROUP_FORMAT_RECIPE_NAME = "Group Recipe";
	
	public GroupFormatRecipe(Recipe recipe, GroupFormatType type) {
		this.ingredients = recipe.getIngredients();
		this.name = DEFAULT_GROUP_FORMAT_RECIPE_NAME;
		this.rating = recipe.getRating();
		
		ingredientGroupAmounts = new HashMap<IngredientGroup, Double>();
		
		if (type == GroupFormatType.SUB) {
			for (MeasuredIngredient ingredient : ingredients) {
				IngredientGroup group = ingredient.getIngredient().getParentGroup();
				if (!ingredientGroupAmounts.containsKey(group)) {
					ingredientGroupAmounts.put(group, 0.0);
				}
				ingredientGroupAmounts.put(group, ingredientGroupAmounts.get(group) + ingredient.getAmountInOunces());
			}
		} else if (type == GroupFormatType.SUPER) {
			for (MeasuredIngredient ingredient : ingredients) {
				IngredientGroup group = ingredient.getIngredient().getParentGroup().getParentGroup();
				if (!ingredientGroupAmounts.containsKey(group)) {
					ingredientGroupAmounts.put(group, 0.0);
				}
				ingredientGroupAmounts.put(group, ingredientGroupAmounts.get(group) + ingredient.getAmountInOunces());
			}
		}
	}
	
	public double getDistanceFromCentroid(GroupFormatType type) {
		double distance = 0.0;
		
		final Set<IngredientGroup> ingredientGroups = getIngredientGroups();
		for(IngredientGroup myIngredientGroup: ingredientGroups){
			StatsSummary ingredientGroupStatsSummary = myIngredientGroup.getStatsSummary();
			double ingredientGroupMean = ingredientGroupStatsSummary.getMean();
			double ingredientGroupStandardDeviation = ingredientGroupStatsSummary.getStandardDeviation();
			double myMeasuredIngredientValueInOunces = getGroupAmounts().get(myIngredientGroup);
			
			double zScore = (myMeasuredIngredientValueInOunces - ingredientGroupMean) / ingredientGroupStandardDeviation;

			if(Math.abs(zScore) >= 2.0){
				distance++;
			}
		}
		distance /= ingredientGroups.size();
		
		return distance;
	} 
	
	public HashMap<IngredientGroup, Double> getGroupAmounts() {
		return ingredientGroupAmounts;
	}
	
	public Set<IngredientGroup> getIngredientGroups() {
		return ingredientGroupAmounts.keySet();
	}
	
	public double calculateEuclideanGroupDistanceFrom(GroupFormatRecipe other, GroupFormatType type) {
		double distance = 0.0;
		double p = 2; //non-linearizing exponent
		int commonIngredients = 0;
		int totalIngredients = 0;
		
		HashMap<IngredientGroup, Double> myIngredientGroupAmounts = new HashMap<IngredientGroup, Double>();
		myIngredientGroupAmounts.putAll(this.ingredientGroupAmounts);
		HashMap<IngredientGroup, Double> otherIngredientGroupAmounts = new HashMap<IngredientGroup, Double>();
		otherIngredientGroupAmounts.putAll(other.ingredientGroupAmounts);
		
		double tmpDistance;
		for(IngredientGroup myIngredientGroup: myIngredientGroupAmounts.keySet()){
			totalIngredients++;
			if(otherIngredientGroupAmounts.containsKey(myIngredientGroup)){
				commonIngredients++;
				tmpDistance = Math.abs(myIngredientGroupAmounts.get(myIngredientGroup) - otherIngredientGroupAmounts.get(myIngredientGroup));
				distance += Math.pow(tmpDistance / myIngredientGroup.getStatsSummary().getStandardDeviation(),p);
				
				otherIngredientGroupAmounts.remove(myIngredientGroup);
			}
			else{
				distance += Math.pow(myIngredientGroupAmounts.get(myIngredientGroup) / myIngredientGroup.getStatsSummary().getStandardDeviation(),p);	
			}
		}
		
		for(IngredientGroup otherIngredientGroup: otherIngredientGroupAmounts.keySet()){
			totalIngredients++;
			distance += Math.pow(otherIngredientGroupAmounts.get(otherIngredientGroup) / otherIngredientGroup.getStatsSummary().getStandardDeviation(),p);				
		}
		
		return (totalIngredients-commonIngredients+1)*Math.pow(distance, 1/p);
	}
}
