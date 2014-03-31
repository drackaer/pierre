/**
 * 
 */
package cooking.recipe.parser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cooking.database.DbManager;
import cooking.database.ItemNotFoundException;
import cooking.recipe.Ingredient;
import cooking.recipe.Measurement;

/**
 * @author Paul Bodily
 *
 */
public class MeasuredIngredientParser {

	private double quantity;
	private Measurement measurement;
	private Ingredient ingredient;
	private List<Measurement> measurements = DbManager.getSingleton().getAllMeasurements();
	private List<Ingredient> ingredients = DbManager.getSingleton().getAllIngredients();
	private Logger ingredientLog = Logger.getLogger(Ingredient.class);
	//private Logger measurementLog = Logger.getLogger(Measurement.class);
	private Logger log = Logger.getLogger(this.getClass());


	
	/**
	 * Constructor which parses string and stores quantity, measurement, and ingredient in instance variables
	 * @throws MeasuredIngredientParsingException 
	 */
	public MeasuredIngredientParser(String string) throws MeasuredIngredientParsingException {
		parseMeasuredIngredient(string);
	}
	
	public MeasuredIngredientParser() {
	}

	/**
	 * Parsing calls reset on the current instance of MeasuredIngredientParser and then attempts to parse the string for measurement, quantity and ingredient
	 * 
	 * Parser assumes that quantity always directly precedes the measurement it modifies
	 * Quantity can be any number followed by any other number, period, or forward slash (e.g. 2 1/2, 2.5, 4)
	 * Largest matching ingredient is returned 
	 * 
	 * @param remainingSearchString string to be parsed for measurement, quantity and ingredient
	 * @throws MeasuredIngredientParsingException 
	 */
	public void parseMeasuredIngredient(String stringToParse) throws MeasuredIngredientParsingException {
		reset();
		
		if(stringToParse.length()<4){
			throw new MeasuredIngredientParsingException("String to parse is less than length 4");
		}
		
		//Algorithm for parsing
		//Loop through the measurements and look for each of them in the string
		String measurementQuantity = parseAndSetMeasurement(stringToParse);
		
		//parse quantity from subsequence prior to measurement
		parseAndSetQuantity(measurementQuantity);
			
		//parse text, looking for a food item (starts most restrictive to least restrictive search)
		parseAndSetIngredient(stringToParse);
		
		if(getMeasurement().getName().equals("whole")){
			measurement.setAmountInOunces(ingredient.getWholeQuantity());
		}
		
		log.debug("For \"" + stringToParse + "\" the following was parsed:\n\t" + quantity + " " + measurement.getName() + " " +
				ingredient.getName() + " (" + quantity*measurement.getAmountInOunces()+ " total ounces) ");
	}


	private void parseAndSetQuantity(String stringWithQuantityInformation){
		if(stringWithQuantityInformation == null){
			setArtificialQuantity();
			return;
		}
		
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile("\\b\\d+[\\.\\s/\\d]*$");
		matcher = pattern.matcher(stringWithQuantityInformation);
		if(!matcher.find()){
			pattern = Pattern.compile("\\b\\d+\\W*$");
			matcher = pattern.matcher(stringWithQuantityInformation);
			if(matcher.find()){
				setQuantity(parseDouble(matcher.group()));
				return;
			}
			setArtificialQuantity();
			return;
		}
		else
			setQuantity(parseDouble(matcher.group()));
		
		//Check for multiplier (i.e. is there a number separated from the quantity by something other than ., /, a word, or a space?)
		pattern = Pattern.compile("\\b\\d+\\W*$");
		matcher = pattern.matcher(stringWithQuantityInformation.substring(0, matcher.start()));
		if(matcher.find()){
			assert(!getMeasurement().isNonspecific());
			assert(getQuantity() != -1);
			this.quantity *= parseDouble(matcher.group());
		}
	}


	private void setArtificialQuantity() {
		setQuantity(1.0);
	}


	private void parseAndSetIngredient(String remainingSearchString)
			throws MeasuredIngredientParsingException {
		Pattern pattern;
		Matcher matcher;
		int bestMatchSize = -1;
		int matchSize = -1; 
		
		for(Ingredient ingredient: ingredients){
			pattern = Pattern.compile(ingredient.getName(), Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(remainingSearchString);
			if(matcher.find()){
				matchSize = matcher.end() - matcher.start();
				if(matchSize > bestMatchSize){
					setIngredient(ingredient);
					bestMatchSize = matchSize;
				}
			}
			for(String alternateName: ingredient.getAlternateNames()){
				pattern = Pattern.compile(alternateName, Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(remainingSearchString);
				if(matcher.find()){
					matchSize = matcher.end() - matcher.start();
					if(matchSize > bestMatchSize){
						setIngredient(ingredient);
						bestMatchSize = matchSize;
					}
				}
			}
		}		
		
		if(bestMatchSize == 0){
			log.warn("Ingredient CSV contains fields with empty values");
		}
		
		if(bestMatchSize == -1 && !remainingSearchString.equals("")){
			ingredientLog.warn("No part of \"" + remainingSearchString + "\" is contained in the ingredients database");
			throw new MeasuredIngredientParsingException("Could not parse ingredient - \""+remainingSearchString+"\" not contained in database");
		}
	}

	
	private String parseAndSetMeasurement(String remainingSearchString) {
		Pattern pattern;
		Matcher matcher;
		
		int matchIndexStart = -1;
		for(Measurement measurement: measurements){
			pattern = Pattern.compile("\\b"+measurement.getName()+"(s|es)?\\b", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(remainingSearchString);
			if(matcher.find()){
				setMeasurement(measurement);
				matchIndexStart = matcher.start();
				break;
			}
		}
		
		if(getMeasurement() == null){
			setArtificialMeasurement(remainingSearchString);
			pattern = Pattern.compile("(\\b\\d+\\W*)?\\b\\d+[\\.\\s/\\d]*");
			matcher = pattern.matcher(remainingSearchString);
			if(matcher.find())
				return matcher.group();
			else{
				pattern = Pattern.compile("(\\.*(salt|spice|pepper)+\\.*)");
				matcher = pattern.matcher(remainingSearchString);
				if(matcher.find()){
					try {
						setMeasurement(DbManager.getSingleton().findMeasurementByName("dash"));
						//measurementLog.warn("FOUND SALT OR SPICE");
					} catch (ItemNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return null;
			}

		}
		return remainingSearchString.substring(0, matchIndexStart);
	}


	private void setArtificialMeasurement(String original) {
		try {
			setMeasurement(DbManager.getSingleton().findMeasurementByName("whole"));
			//measurementLog.warn("NO MEASUREMENT PARSED IN \"" + original + "\"");
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
		}		
	}


	private double parseDouble(String group) {
		double returnDouble = 0;
		String parseString = group;
		Pattern pattern = Pattern.compile("\\d+\\s?/\\s?\\d+");
		Matcher matcher = pattern.matcher(parseString);
		
		
		//NEEDS TO CONSIDER ALL FORMATS FOR QUANTITY
		if(matcher.find()){
			//fraction
			String fraction = matcher.group();
			int splitIndex = fraction.indexOf('/');
			double numerator = Double.parseDouble(fraction.substring(0, splitIndex));
			double denominator = Double.parseDouble(fraction.substring(splitIndex+1));
			returnDouble += numerator / denominator;
			parseString = parseString.substring(0,matcher.start());
		}

		pattern = Pattern.compile("\\d+(\\.\\d+)?");
		matcher = pattern.matcher(parseString);
		
		if(matcher.find())
			returnDouble += Double.parseDouble(matcher.group());
		
		return returnDouble;
	}


	/**
	 * Sets quantity to -1, measurement and ingredient to null; 
	 */
	private void reset() {
		quantity = -1;
		measurement = null;
		ingredient = null; 
	}

	/**
	 * @return the quantity
	 */
	public double getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the measurement
	 */
	public Measurement getMeasurement() {
		return measurement;
	}

	/**
	 * @param measurement the measurement to set
	 */
	public void setMeasurement(Measurement measurement) {
		this.measurement = measurement;
	}

	/**
	 * @return the ingredient
	 */
	public Ingredient getIngredient() {
		return ingredient;
	}

	/**
	 * @param ingredient the ingredient to set
	 */
	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}
}
