/**
 * 
 */
package cooking.recipe.parser;

import cooking.database.DbManager;
import cooking.database.ItemNotFoundException;
import cooking.recipe.Ingredient;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Measurement;

/**
 * @author Norkish
 * 
 */
public class MeasurementConverter {

	public double convertToOunces(double quantity, Measurement measurement,
			Ingredient ingredient) {
		return quantity * measurement.getAmountInOunces();
	}

	public MeasuredIngredient convertToHumanReadable(MeasuredIngredient measuredIngredient) {
		MeasuredIngredient returnMeasuredIngredient = new MeasuredIngredient();

		Ingredient newIngredient = measuredIngredient.getIngredient();
		double quantity = measuredIngredient.getAmountInOunces();
		Measurement newMeasurement = null;
		
		try {
			newMeasurement = DbManager.getSingleton().findMeasurementByName("ounce");;

			if (newIngredient.getName().equals("bay leaf")) {
				newMeasurement = DbManager.getSingleton().findMeasurementByName("leaves");
				quantity /= newMeasurement.getAmountInOunces();
			}
			else if (newIngredient.getName().equals("garlic")) {
				newMeasurement = DbManager.getSingleton().findMeasurementByName("clove");
				quantity /= newMeasurement.getAmountInOunces();
			}
			else if (newIngredient.getName().equals("cinnamon")) {
				newMeasurement = DbManager.getSingleton().findMeasurementByName("stick");
				quantity /= newMeasurement.getAmountInOunces();
			}
			else if (newIngredient.getName().equals("bacon")) {
				newMeasurement = DbManager.getSingleton().findMeasurementByName("slice");
				quantity /= newMeasurement.getAmountInOunces();
			}
			else if (newIngredient.getName().equals("beef bouillon") || newIngredient.getName().equals("chicken bouillon")) {
				newMeasurement = DbManager.getSingleton().findMeasurementByName("cube");
				quantity /= newMeasurement.getAmountInOunces();
			}
			else if (newIngredient.getParentGroup().getParentGroup().getName().equals("Meats")) {
				Measurement pound = DbManager.getSingleton().findMeasurementByName("pound");
				if(quantity >= pound.getAmountInOunces()){
					newMeasurement = pound;
					quantity /= newMeasurement.getAmountInOunces();
				}
			}
			else if (newIngredient.getParentGroup().getName().equals("Chilis")) {
				double chiliOunces = DbManager.getSingleton().findIngredientGroupByName("Fruits and Vegetables").findChildGroupByName("Chilis").getWholeQuantity();
				newMeasurement = DbManager.getSingleton().findMeasurementByName("whole");
				quantity /= chiliOunces;
			}
			else{
				Measurement dash = DbManager.getSingleton().findMeasurementByName("dash");
				Measurement tsp = DbManager.getSingleton().findMeasurementByName("teaspoon");
				Measurement tbsp = DbManager.getSingleton().findMeasurementByName("tablespoon");
				Measurement cup = DbManager.getSingleton().findMeasurementByName("cup");
				Measurement quart = DbManager.getSingleton().findMeasurementByName("quart");
				if(quantity >= quart.getAmountInOunces()){
					newMeasurement = quart;
				}
				else if(quantity >= cup.getAmountInOunces()/3){
					newMeasurement = cup;
				}
				else if(quantity >= tbsp.getAmountInOunces()){
					newMeasurement = tbsp;
				}
				else if(quantity >= tsp.getAmountInOunces()){
					newMeasurement = tsp;
				}
				else if(quantity >= dash.getAmountInOunces()){
					newMeasurement = dash;
				}
				quantity /= newMeasurement.getAmountInOunces();
			}
		} catch (ItemNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		returnMeasuredIngredient.setMeasurement(newMeasurement);
		returnMeasuredIngredient.setQuantity(quantity);
		returnMeasuredIngredient.setIngredient(newIngredient);

		return returnMeasuredIngredient;
	}
}
