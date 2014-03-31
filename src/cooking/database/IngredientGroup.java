/**
 * 
 */
package cooking.database;

import java.util.ArrayList;
import java.util.List;

import cooking.recipe.Ingredient;
import cooking.recipe.stats.Aggregator;
import cooking.recipe.stats.StatsSummary;

/**
 * @author Norkish
 * 
 */
public class IngredientGroup {

	private String name;
	private double wholeQuantity = 4.0; // This value should never actually be read, but just in case.
	private IngredientGroup parentGroup;
	private List<IngredientGroup> childrenGroups = new ArrayList<IngredientGroup>();
	private List<Ingredient> ingredients = new ArrayList<Ingredient>();
	private StatsSummary statsSummary = null;
	
	public void loadFromCsvFormat(String input) {

		String[] parts = input.split(",");

		setName(parts[0]);
		if(parts.length > 1)
			setWholeQuantity(Double.parseDouble(parts[1]));
		else
			if(getParentGroup() != null)
				setWholeQuantity(getParentGroup().getWholeQuantity());
	}

	public void setWholeQuantity(double wholeQuantity) {
		this.wholeQuantity = wholeQuantity;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParentGroup(IngredientGroup parentGroup) {
		this.parentGroup = parentGroup;
		parentGroup.addChildGroup(this);
	}

	public void addChildGroup(IngredientGroup childrenGroup) {
		this.childrenGroups.add(childrenGroup);
	}

	public boolean isTopGroup(){
		return hasParents();
	}

	public boolean hasParents() {
		return (parentGroup != null);
	}

	public List<IngredientGroup> getChildrenGroups() {
		return childrenGroups;
	}

	public String getName() {
		return name;
	}

	public double getWholeQuantity() {
		return wholeQuantity;
	}

	public IngredientGroup getParentGroup() {
		return parentGroup;
	}

	public void addIngredient(Ingredient ingredient) {
		this.ingredients.add(ingredient);
	}
	
	/**
	 * @return a list containing all ingredients in this and all children groups
	 */
	public List<Ingredient> getAllIngredients(){
		List<Ingredient> allIngredients = new ArrayList<Ingredient>();
		allIngredients.addAll(getImmediateIngredients());
		for(IngredientGroup group: childrenGroups){
			allIngredients.addAll(group.getAllIngredients());
		}
		
		return allIngredients;
	}

	private List<Ingredient> getImmediateIngredients() {
		return ingredients;
	}
	
	public boolean containsIngredient(Ingredient ingredient){
		if (ingredients.contains(ingredient))
			return true;
		else{
			for(IngredientGroup group: childrenGroups){
				if(group.containsIngredient(ingredient))
					return true;
			}
		}			
			
		return false;
	}

	public IngredientGroup findChildGroupByName(String name) throws ItemNotFoundException {
		IngredientGroup group = null;
		
		for (IngredientGroup cur : childrenGroups) {
			if (cur.getName().equals(name)) {
				group = cur;
				break;
			}
		}
		
		if (group == null) {
			throw new ItemNotFoundException();
		}
		
		return group;
	}

	/**
	 * Gets the stats of this ingredient group, this will be calculated the first time and then
	 * cached for future calls.
	 * 
	 * Note: this method is not threadsafe.
	 * 
	 * @return
	 */
	public StatsSummary getStatsSummary() {

		if (statsSummary == null) {
			Aggregator agg = new Aggregator();
			statsSummary = agg.ComputeStats(this);
		}
		
		return statsSummary;
	}
	
	/**
	 * Clears the cached stats object, so it will be recomputed on next get.  Note that this
	 * only clears the stats summary for this specific group but the ingredients summary from
	 * which it is generated is not cleared at this time.  That can be cleared specifically
	 * with a separate method on the Aggregator class.
	 * 
	 * @return
	 */
	public void clearStatsSummary() {
		statsSummary = null;
	}
	
	
}
