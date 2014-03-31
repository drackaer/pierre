package cooking.database;


public class GroupReport {
	private IngredientGroup group;
	private double totalAmountInOunces;
	private int numberOfUniqueIngredients;
	
	public GroupReport(IngredientGroup group, double amount, int number) {
		this.group = group;
		totalAmountInOunces = amount;
		numberOfUniqueIngredients = number;
	}
	
	public IngredientGroup getGroup() {
		return group;
	}
	public void setGroup(IngredientGroup group) {
		this.group = group;
	}
	public double getTotalAmountInOunces() {
		return totalAmountInOunces;
	}
	public void setTotalAmountInOunces(double totalAmountInOunces) {
		this.totalAmountInOunces = totalAmountInOunces;
	}
	public int getNumberOfUniqueIngredients() {
		return numberOfUniqueIngredients;
	}
	public void setNumberOfUniqueIngredients(int numberOfUniqueIngredients) {
		this.numberOfUniqueIngredients = numberOfUniqueIngredients;
	}
}
