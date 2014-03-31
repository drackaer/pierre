package cooking.recipe;

import java.util.ArrayList;
import java.util.List;

import cooking.database.DbManager;
import cooking.database.IngredientGroup;
import cooking.database.ItemNotFoundException;

public class Ingredient implements ICsvSerializable {

	public Ingredient() {
		alternateNames = new ArrayList<String>();
	}
	
	private String name;
	private List<String> alternateNames;
	private IngredientGroup parentGroup;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}

	@Override
	public String convertToCsvFormat() {
		String str = getName();
		
		for (String alt : alternateNames) {
			str += "," + alt;
		}
		
		return str;
	}

	@Override
	public void loadFromCsvFormat(String input) {
		alternateNames = new ArrayList<String>();
		
		if (input.contains(",")) {
			String[] parts = input.split(",");
			
			setName(parts[0].trim());
			
			for (int i = 1; i < parts.length; i++) {
				alternateNames.add(parts[i].trim());
			}
		}
		else {
			setName(input);
		}
	}
	
	public static Ingredient loadByName(String name) throws ItemNotFoundException {
		return DbManager.getSingleton().findIngredientByName(name);
	}
	
	public static Ingredient loadByNameOrAnyVariant(String name) throws ItemNotFoundException {
		return DbManager.getSingleton().findIngredientByNameOrAnyVariant(name);
	}

	public void setAlternateNames(List<String> alternateNames) {
		this.alternateNames = alternateNames;
	}

	public List<String> getAlternateNames() {
		return alternateNames;
	}
	
	/**
	 * Checks to see if the given str exactly matches the name or any alternate names, ignoring case. 
	 * 
	 * @param str
	 * @return
	 */
	public boolean matchesAnyVariant(String str) {
		boolean isMatch = false;
		
		if (getName().toLowerCase().equals(str.toLowerCase())) {
			isMatch = true;
		}
		else {
			for (String curStr : alternateNames) {
				if (curStr.toLowerCase().equals(str.toLowerCase())) {
					isMatch = true;
					break;
				}
			}
		}
		
		return isMatch;
	}
	
	/**
	 * Checks to see if any of the names (including alternates) are contained in the given string
	 * 
	 * @param str
	 * @return
	 */
	public boolean anyVariantContainedIn(String str) {
		boolean contains = false;
		
		if (str.toLowerCase().contains(getName().toLowerCase())) {
			contains = true;
		}
		else {
			for (String curStr : alternateNames) {
				if (str.toLowerCase().contains(curStr.toLowerCase())) {
					contains = true;
					break;
				}
			}
		}
		
		return contains;
	}

	public double getDistanceWeight() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean equals(Object other) {
		boolean isEqual = false;

		try {
			Ingredient otherIng = (Ingredient)other;
			
			if (otherIng.getName().equals(getName()) && this.getName().length() != 0) {
				isEqual = true;
			}
		}
		catch (Exception e){
			isEqual = false;
		}
		
		return isEqual;
	}
	
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	public void setParentGroup(IngredientGroup parentGroup) {
		this.parentGroup = parentGroup;
		parentGroup.addIngredient(this);
	}
	
	public IngredientGroup getParentGroup(){
		return parentGroup;
	}

	public double getWholeQuantity() {
		//TODO May want this to be more specific to ingredient
		return parentGroup.getWholeQuantity();
	}
}
