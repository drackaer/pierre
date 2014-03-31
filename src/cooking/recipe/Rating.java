package cooking.recipe;

import java.io.IOException;

public class Rating implements ICsvSerializable {
	private double numericalRating;
	private int numberOfRatings;
	private int numberOfTimesSaved;
	private String recipeName;
	
	private static final int DEFAULT_RATING = -1;
	private static final int DEFAULT_NUMBER_OF_RATINGS = -1;
	private static final int DEFAULT_NUMBER_OF_TIMES_SAVED = -1;
	public static final double RANDOM_RATING = 0.01;
	
	public Rating(String recipeName) {
		this.recipeName = recipeName;
		initialize();
	}
	
	public Rating(String recipeName, double numericalRating) {
		this.recipeName = recipeName;
		initialize();
		this.numericalRating = numericalRating;
	}
	
	public Rating(String recipeName, double numericalRating, int numberOfRatings) {
		this.recipeName = recipeName;
		initialize();
		this.numericalRating = numericalRating;
		this.numberOfRatings = numberOfRatings;
	}
	
	public Rating(String recipeName, double numericalRating, int numberOfRatings, int numberOfTimesSaved) {
		this.recipeName = recipeName;
		initialize();
		this.numericalRating = numericalRating;
		this.numberOfRatings = numberOfRatings;
		this.numberOfTimesSaved = numberOfTimesSaved;
	}
	
	private void initialize() {
		setNumericalRating(DEFAULT_RATING);
		setNumberOfRatings(DEFAULT_NUMBER_OF_RATINGS);
		setNumberOfTimesSaved(DEFAULT_NUMBER_OF_TIMES_SAVED);
	}

	public void setNumericalRating(double numericalRating) {
		this.numericalRating = numericalRating;
	}

	public double getNumericalRating() {
		return numericalRating;
	}

	public void setNumberOfRatings(int numberOfRatings) {
		this.numberOfRatings = numberOfRatings;
	}

	public int getNumberOfRatings() {
		return numberOfRatings;
	}

	public void setNumberOfTimesSaved(int numberOfTimesSaved) {
		this.numberOfTimesSaved = numberOfTimesSaved;
	}

	public int getNumberOfTimesSaved() {
		return numberOfTimesSaved;
	}
	
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		output.append("\nRating:" + numericalRating);
		output.append("\nNumber of ratings:" + numberOfRatings);
		output.append("\nNumber of times saved:" + numberOfTimesSaved);
		
		return output.toString();
	}

	@Override
	public String convertToCsvFormat() {
		return recipeName + "," + numericalRating + "," + numberOfRatings + "," + numberOfTimesSaved;
	}

	@Override
	public void loadFromCsvFormat(String input) throws IOException {
		String[] splitLine = input.split(",");
		
		int nameEndsAt = splitLine.length - 3;
		// Need to account for commas in the recipe name
		StringBuffer name = new StringBuffer();
		
		for (int i = 0; i < nameEndsAt; i++) {
			name.append(splitLine[i]);
			
			if (i+1 != nameEndsAt) {
				name.append(",");
			}
		}
	
		recipeName = name.toString();
		numericalRating = Double.parseDouble(splitLine[nameEndsAt]);
		numberOfRatings = Integer.parseInt(splitLine[nameEndsAt + 1]);
		numberOfTimesSaved = Integer.parseInt(splitLine[nameEndsAt + 2]);
	}
	
	public String getRecipeName() {
		return recipeName;
	}
}
