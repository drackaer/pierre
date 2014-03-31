package cooking.recipe;

import java.io.IOException;

import cooking.database.DbManager;
import cooking.database.ItemNotFoundException;

public class Measurement implements ICsvSerializable {

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amountInOunces);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Measurement other = (Measurement) obj;
		if (Double.doubleToLongBits(amountInOunces) != Double
				.doubleToLongBits(other.amountInOunces))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public static final String OUNCES = "ounce";
	
	private String name;
	private double amountInOunces;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setAmountInOunces(double amountInOunces) {
		this.amountInOunces = amountInOunces;
	}

	public double getAmountInOunces() {
		return amountInOunces;
	}
	
	public boolean isNonspecific(){
		return amountInOunces == -1;
	}
	
	public String toString() {
		return getName();
	}
	
	@Override
	public String convertToCsvFormat() {
		String str = getName() + "," + getAmountInOunces();
		return str;
	}

	@Override
	public void loadFromCsvFormat(String input) throws IOException {
		String[] parts = input.split(",");
		
		if (parts.length == 2) {
			setName(parts[0]);
			setAmountInOunces(Double.valueOf(parts[1]));
		}
		else {
			throw new IOException("Could not parse the given input string");
		}
	}
	
	public static Measurement loadByName(String name) throws ItemNotFoundException {
		return DbManager.getSingleton().findMeasurementByName(name);
	}
}
