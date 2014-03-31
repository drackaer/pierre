package cooking.recipe.distance;

import cooking.recipe.Recipe;

public class RecipeDistanceObject implements Comparable<RecipeDistanceObject>{
	Recipe subjectRecipe;
	Recipe queryRecipe;
		
	double distance;
	double percentDistantOfGroupMax;
	/**
	 * @param recipeA
	 * @param recipeB
	 * @param distance
	 * @param percentDistantOfGroupMax
	 */
	public RecipeDistanceObject(Recipe recipeA, Recipe recipeB,
			double distance) {
		this.subjectRecipe = recipeA;
		this.queryRecipe = recipeB;
		this.distance = distance;
	}
	/**
	 * @return the recipeA
	 */
	public Recipe getSubjectRecipe() {
		return subjectRecipe;
	}
	/**
	 * @param recipeA the recipeA to set
	 */
	public void setSubjectRecipe(Recipe recipeA) {
		this.subjectRecipe = recipeA;
	}
	/**
	 * @return the recipeB
	 */
	public Recipe getQueryRecipe() {
		return queryRecipe;
	}
	/**
	 * @param recipeB the recipeB to set
	 */
	public void setQueryRecipe(Recipe recipeB) {
		this.queryRecipe = recipeB;
	}
	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}
	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}
	/**
	 * @return the percentDistantOfGroupMax
	 */
	public double getPercentDistantOfGroupMax() {
		return percentDistantOfGroupMax;
	}
	/**
	 * @param percentDistantOfGroupMax the percentDistantOfGroupMax to set
	 */
	public void setPercentDistantOfGroupMax(double percentDistantOfGroupMax) {
		this.percentDistantOfGroupMax = percentDistantOfGroupMax;
	}
	@Override
	public int compareTo(RecipeDistanceObject other) {
		if (this.distance - other.distance > 0)
			return 1;
		else if (this.distance - other.distance < 0)
			return -1;
		return 0;
	}
	
}
