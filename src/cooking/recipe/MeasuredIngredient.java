package cooking.recipe;

import java.io.IOException;

import cooking.database.ItemNotFoundException;
import cooking.recipe.parser.MeasuredIngredientParser;
import cooking.recipe.parser.MeasuredIngredientParsingException;
import cooking.recipe.parser.MeasurementConverter;

public class MeasuredIngredient implements ICsvSerializable, Cloneable {
	private double quantity;
	private Measurement measurement;
	private Ingredient ingredient;
	private static MeasurementConverter measurementConverter;

	public MeasuredIngredient() {}
	
	public MeasuredIngredient(String string) throws MeasuredIngredientParsingException {
		MeasuredIngredientParser measuredIngredientParser = new MeasuredIngredientParser(string);
		quantity = measuredIngredientParser.getQuantity();
		measurement = measuredIngredientParser.getMeasurement();
		ingredient = measuredIngredientParser.getIngredient();
	}
	
	// Used when generating random recipes
	public MeasuredIngredient(Ingredient ingredient, double quantity) throws ItemNotFoundException {
		this.quantity = quantity;
		this.ingredient = ingredient;
		this.measurement = Measurement.loadByName(Measurement.OUNCES);
	}
	
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setMeasurement(Measurement measurement) {
		this.measurement = measurement;
	}
	public Measurement getMeasurement() {
		return measurement;
	}
	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}
	public Ingredient getIngredient() {
		return ingredient;
	}
	
	public double getAmountInOunces() {
		// TODO: need to make sure measurement converter is initialized first
		// Throwing in an initializing statement here so that I can test the ARFF Converter.
		if (measurementConverter == null) {
			measurementConverter = new MeasurementConverter();
		}
		double ounces = measurementConverter.convertToOunces(quantity, measurement, ingredient);
		
		return ounces;
	}

	public MeasuredIngredient getHumanReadableMeasuredIngredient() {
		// TODO: need to make sure measurement converter is initialized first
		// Throwing in an initializing statement here so that I can test the ARFF Converter.
		if (measurementConverter == null) {
			measurementConverter = new MeasurementConverter();
		}
		MeasuredIngredient measuredIngredient = measurementConverter.convertToHumanReadable(this);
		
		return measuredIngredient;
	}
	
	public String toString() {
		//return quantity + " " + measurement.toString() + " - " + ingredient.toString();
		return roundToTwoDecimals(quantity) + " " + measurement.toString() + (quantity <= 1.0?"":"s") + " - " + ingredient.toString();
	}

	@Override
	public String convertToCsvFormat() {
		//return quantity + "," + measurement.getName() + "," + ingredient.getName();
		return roundToTwoDecimals(quantity) + "," + measurement.getName() + "," + ingredient.getName();
	}
	
	private double roundToTwoDecimals(double d) {
		double rounded = d * 100;
		rounded = Math.round(rounded);
		rounded /= 100;
		
		return rounded;
	}

	@Override
	public void loadFromCsvFormat(String input) throws IOException {
		String[] parts = input.split(",");
		
		if (parts.length == 3) {
			setQuantity(Double.valueOf(parts[0]));

			try {
				setMeasurement(Measurement.loadByName(parts[1]));
				setIngredient(Ingredient.loadByNameOrAnyVariant(parts[2]));
			} catch (ItemNotFoundException e) {
				throw new IOException("Error parsing measurements and ingredients.  Trying to load measurement: '" + parts[1] + "' and ingredient '" + parts[2] + "'", e);
			}
		}
		else {
			throw new IOException("Could not parse the given input string");
		}
	}

	public void setAmountInOunces(double amount) throws ItemNotFoundException {
		Measurement ouncesMeasurement = Measurement.loadByName(Measurement.OUNCES);
		setMeasurement(ouncesMeasurement);
		setQuantity(amount);
	}

	@Override
	public MeasuredIngredient clone() {
		MeasuredIngredient newIng = new MeasuredIngredient();
		
		newIng.setIngredient(getIngredient());
		newIng.setMeasurement(getMeasurement());
		newIng.setQuantity(getQuantity());
		
		return newIng;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ingredient == null) ? 0 : ingredient.hashCode());
		result = prime * result
				+ ((measurement == null) ? 0 : measurement.hashCode());
		long temp;
		temp = Double.doubleToLongBits(quantity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeasuredIngredient other = (MeasuredIngredient) obj;
		if (ingredient == null) {
			if (other.ingredient != null)
				return false;
		} else if (!ingredient.equals(other.ingredient))
			return false;
		if (measurement == null) {
			if (other.measurement != null)
				return false;
		} else if (!measurement.equals(other.measurement))
			return false;
		if (Double.doubleToLongBits(quantity) != Double
				.doubleToLongBits(other.quantity))
			return false;
		return true;
	}

	public String toOnlineString() {
		String str = "";
		
		str += "<i>" + toString() + "</i>";
		return str;
	}
}
