package cooking.recipe;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cooking.database.DbManager;
import cooking.database.GroupReport;
import cooking.database.IngredientGroup;
import cooking.database.ItemNotFoundException;
import cooking.evaluation.Evaluation;
import cooking.recipe.parser.MeasuredIngredientParsingException;
import cooking.recipe.stats.Aggregator;
import cooking.recipe.stats.StatsSummary;

public class Recipe implements ICsvSerializable {
	protected String name;
	protected List<MeasuredIngredient> ingredients;
	protected Rating rating;

	protected Evaluation temporaryEvaluation = null;

	public static final String DEFAULT_NAME = "Pierre's Special Surprise";

	public Recipe() {
		this.name = DEFAULT_NAME;
		this.rating = new Rating(this.name);
	}

	public Recipe(String name, List<String> ingredients, Rating rating) {
		this.name = name;
		this.rating = rating;
		this.ingredients = new ArrayList<MeasuredIngredient>();

		for (String ingredient : ingredients) {
			try {
				this.ingredients.add(new MeasuredIngredient(ingredient));
			} catch (MeasuredIngredientParsingException e) {
				// e.printStackTrace();
			}
		}
	}

	// Constructor for random recipes
	public Recipe(List<MeasuredIngredient> ingredients) {
		this.name = DEFAULT_NAME;
		this.rating = new Rating(this.name, Rating.RANDOM_RATING);
		this.ingredients = ingredients;
	}

	public boolean cooksTheSameThingAs(Recipe other) {
		return (ingredients.containsAll(other.ingredients) && other.ingredients
				.containsAll(ingredients));
	}

	public void setIngredients(List<MeasuredIngredient> ingredients) {
		this.ingredients = ingredients;
	}

	public List<MeasuredIngredient> getIngredients() {
		return ingredients;
	}

	public List<MeasuredIngredient> getIngredientsOrderedByAmount() {
		List<MeasuredIngredient> orderedIngredients = new ArrayList<MeasuredIngredient>();

		for (MeasuredIngredient mi : getIngredients()) {
			int i = 0;
			for (; i < orderedIngredients.size(); i++) {
				if (mi.getAmountInOunces() > orderedIngredients.get(i)
						.getAmountInOunces())
					break;
			}
			orderedIngredients.add(i, mi);
		}

		return orderedIngredients;
	}

	protected final String DEFAULT_DIRECTIONS = "Combine ingredients and bring to boil.  Reduce heat and simmer until done, stirring occasionally. Serve piping hot and enjoy.";

	public String getDirections() {
		return DEFAULT_DIRECTIONS;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		if (name.equals(DEFAULT_NAME) && getIngredients().size() > 0) {
			NameGenerator nameGen = new NameGenerator();
			name = nameGen.generateName(this);
		}

		return name;
	}
	
	public void clearName() {
		name = DEFAULT_NAME;
	}

	public String toLongString() {
		String str = "";
		str += getName() + "\n";

		if (temporaryEvaluation != null) {
			str += "Evaluation: " + temporaryEvaluation.getStringEvaluation() + "\n";
		}
		str += "Ingredients:\n";

		for (MeasuredIngredient mi : getIngredientsOrderedByAmount()) {
			str += "  " + mi.getHumanReadableMeasuredIngredient().toString()
					+ "\n";
		}

		str += "\nDirections:\n";
		str += getDirections();

		return str;
	}

	public static final String CSV_HEADER = "Recipe:\n";

	@Override
	public String convertToCsvFormat() {
		String str = CSV_HEADER;
		str += getName() + "\n";

		for (MeasuredIngredient mi : getIngredients()) {
			str += mi.convertToCsvFormat() + "\n";
		}

		return str;
	}

	@Override
	public void loadFromCsvFormat(String input) throws IOException {
		String[] parts = input.split("\n");

		if (parts.length < 3 || !parts[0].equals(CSV_HEADER.trim())) {
			throw new IOException("Could not parse the given input string");
		}

		setName(parts[1]);

		ingredients = new ArrayList<MeasuredIngredient>();
		for (int i = 2; i < parts.length; i++) {
			MeasuredIngredient mi = new MeasuredIngredient();
			mi.loadFromCsvFormat(parts[i]);
			ingredients.add(mi);
		}
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public Rating getRating() {
		return rating;
	}

	@Override
	public Recipe clone() {
		Recipe newRecipe = new Recipe();

		List<MeasuredIngredient> list = new ArrayList<MeasuredIngredient>();
		list.addAll(getIngredients());

		newRecipe.setIngredients(list);
		//newRecipe.setTemporaryEvaluation(temporaryEvaluation, temporaryEvaluationString);

		return newRecipe;
	}

	/**
	 * Returns a new scaled version of the recipe such that the total volume is
	 * the passed totalOunces.
	 * 
	 * @param totalOunces
	 * @return
	 * @throws ItemNotFoundException
	 *             if the "ounces" measurement cannot be found
	 */
	public Recipe getScaledVersion(double totalOunces)
			throws ItemNotFoundException {
		Recipe recipe = this.clone();
		recipe.setName(this.getName());

		double originalTotal = getTotalVolume();
		double scaleFactor = totalOunces / originalTotal;

		Measurement ouncesMeasurement = Measurement.loadByName(Measurement.OUNCES);

		for (MeasuredIngredient mi : recipe.getIngredients()) {
			double oldAmountInOunces = mi.getAmountInOunces();
			double newAmountInOunces = oldAmountInOunces * scaleFactor;

			mi.setMeasurement(ouncesMeasurement);
			mi.setQuantity(newAmountInOunces);
		}

		return recipe;
	}

	/**
	 * Returns the total volume of the recipe in ounces.
	 * 
	 * @return
	 */
	public double getTotalVolume() {
		double volume = 0;

		for (MeasuredIngredient mi : this.getIngredients()) {
			volume += mi.getAmountInOunces();
		}

		return volume;
	}

	public double getDistanceFromCentroid(){
		double distance = 0.0;
		double localIngredientDistance = 0.0;
		double ingredientCountDistance = 0.0;
		double subIngredientDistance = 0.0;
		double superIngredientDistance = 0.0;
		
		//Map<Ingredient, StatsSummary> statsMap = Aggregator.getIngredientStatsMap();
		
		final List<MeasuredIngredient> myIngredients = this.getIngredients();
		for(MeasuredIngredient myMeasuredIngredient: myIngredients){
			Ingredient ingredient = myMeasuredIngredient.getIngredient();
			//StatsSummary ingredientsStatsSummary = statsMap.get(ingredient);
			StatsSummary ingredientsStatsSummary = Aggregator.getSmoothedStatsObject(ingredient); 
			double ingredientMean = ingredientsStatsSummary.getMean();
			double ingredientStandardDeviation = ingredientsStatsSummary.getStandardDeviation();
			double myMeasuredIngredientValueInOunces = myMeasuredIngredient.getAmountInOunces();
			
			double zScore = (myMeasuredIngredientValueInOunces - ingredientMean) / ingredientStandardDeviation;

			if(Math.abs(zScore) >= 2.0){
				localIngredientDistance++;
			}
		}
		
		localIngredientDistance /= myIngredients.size();
		
		int ingredientCountDeltaFromMean = Math.abs(myIngredients.size() - mean_ingredient_count);
		ingredientCountDistance = 1 - 1 / (countSpread * ingredientCountDeltaFromMean + 1);
		
		GroupFormatRecipe thisGroupFormatSubRecipe = new GroupFormatRecipe(this, GroupFormatType.SUB);
		subIngredientDistance = thisGroupFormatSubRecipe.getDistanceFromCentroid(GroupFormatType.SUB);
		GroupFormatRecipe thisGroupFormatSuperRecipe = new GroupFormatRecipe(this, GroupFormatType.SUPER);
		superIngredientDistance = thisGroupFormatSuperRecipe.getDistanceFromCentroid(GroupFormatType.SUPER);
		
		distance = localIngredientDistance * localIngredientDistanceWeight
			+ ingredientCountDistance * ingredientCountDistanceWeight
			+ subIngredientDistance * subIngredientDistanceWeight
			+ superIngredientDistance * superIngredientDistanceWeight;
		
		return distance;
	}

	final static int mean_ingredient_count = 11;
	final static double countSpread = 1/3; // Larger the fraction, penalizes variance from mean ingredient count more
	final static double localIngredientDistanceWeight = 0.25;
	final static double ingredientCountDistanceWeight = 0.25;
	final static double subIngredientDistanceWeight = 0.25;
	final static double superIngredientDistanceWeight = 0.25;
	
	
	/**
	 * Returns the weighted Euclidean distance from the specified recipe
	 * 
	 * Distance is calculated by summing the products of the difference in
	 * ounces for identical ingredients and the ingredient's distance weight all
	 * raised to the power p where p is a non-linearizing exponent
	 * 
	 * ex: ((distance_1*weight_1)^p + ... + (distance_i*weight_i))^(1/p) where i
	 * indicates the ingredient
	 * 
	 * @param other
	 * @return Euclidean distance
	 */
	public double calculateEuclideanDistanceFrom(Recipe other) {
		double distance = 0.0;
		double p = 2; // non-linearizing exponent
		int totalIngredients = 0;
		int commonIngredients = 0;

		List<MeasuredIngredient> myMeasuredIngredients = new ArrayList<MeasuredIngredient>(
				this.getIngredients());
		List<MeasuredIngredient> otherMeasuredIngredients = new ArrayList<MeasuredIngredient>(
				other.getIngredients());
		Map<Ingredient, StatsSummary> statsMap = Aggregator
				.getIngredientStatsMap();

		double tmpDistance;
		boolean matchFound;
		for (MeasuredIngredient myMeasuredIngredient : myMeasuredIngredients) {
			totalIngredients++;
			double myStdDev = 1;
			if (statsMap.containsKey(myMeasuredIngredient.getIngredient())) {
				myStdDev = statsMap.get(myMeasuredIngredient.getIngredient())
						.getStandardDeviation();
			}

			if (myStdDev == 0) {
				// avoid divide by zero errors
				myStdDev = 1;
			}

			matchFound = false;
			for (int i = 0; i < otherMeasuredIngredients.size() && !matchFound; i++) {
				MeasuredIngredient otherMeasuredIngredient = otherMeasuredIngredients
						.get(i);
				if (myMeasuredIngredient.getIngredient().equals(
						otherMeasuredIngredient.getIngredient())) {
					// found same ingredient, now determine distance

					tmpDistance = Math.abs(myMeasuredIngredient
							.getAmountInOunces()
							- otherMeasuredIngredient.getAmountInOunces());
					distance += Math.pow(tmpDistance / myStdDev, p);

					otherMeasuredIngredients.remove(i);
					matchFound = true;
					commonIngredients++;
				}
			}
			if (!matchFound) {
				distance += Math.pow(myMeasuredIngredient.getAmountInOunces()
						/ myStdDev, p);
			}
		}

		// all remaining mismatches
		for (MeasuredIngredient otherMeasuredIngredient : otherMeasuredIngredients) {
			totalIngredients++;
			double otherStdDev = 1;

			if (statsMap.containsKey(other.getIngredients())) {
				otherStdDev = statsMap.get(
						otherMeasuredIngredient.getIngredient())
						.getStandardDeviation();
			}

			if (otherStdDev == 0) {
				// avoid divide by zero errors
				otherStdDev = 1;
			}

			distance += Math.pow(otherMeasuredIngredient.getAmountInOunces()
					/ otherStdDev, p);
		}

		return (totalIngredients - commonIngredients + 1)
				* Math.pow(distance, 1 / p);
	}

	public double calculateInterpolatedDistanceFrom(Recipe other) {
		double interpolatedDistance = 0;

		double localWeight = .15;
		double subGroupWeight = .35;
		double superGroupWeight = .50;

		interpolatedDistance += localWeight
				* calculateEuclideanDistanceFrom(other);
		interpolatedDistance += subGroupWeight
				* calculateEuclideanGroupDistanceFrom(other,
						GroupFormatType.SUB);
		interpolatedDistance += superGroupWeight
				* calculateEuclideanGroupDistanceFrom(other,
						GroupFormatType.SUPER);

		return interpolatedDistance;
	}

	public double calculateEuclideanGroupDistanceFrom(Recipe other,
			GroupFormatType type) {
		GroupFormatRecipe thisGroupFormatRecipe = new GroupFormatRecipe(this,
				type);
		GroupFormatRecipe otherGroupFormatRecipe = new GroupFormatRecipe(other,
				type);

		return thisGroupFormatRecipe.calculateEuclideanGroupDistanceFrom(
				otherGroupFormatRecipe, type);
	}

	/**
	 * Looks for the same ingredient to show up twice in the list and combines
	 * it to a single instance of a MeasuredIngredient
	 * 
	 * @throws ItemNotFoundException
	 *             if the "ounces" measurement could not be found
	 */
	public void combineSameIngredients() throws ItemNotFoundException {
		List<MeasuredIngredient> newList = new ArrayList<MeasuredIngredient>();

		for (MeasuredIngredient mi : getIngredients()) {
			boolean foundInList = false;

			for (MeasuredIngredient newListMi : newList) {
				if (newListMi.getIngredient().equals(mi.getIngredient())) {
					foundInList = true;
					newListMi.setAmountInOunces(newListMi.getAmountInOunces()
							+ mi.getAmountInOunces());
					break;
				}
			}

			if (!foundInList) {
				newList.add(mi);
			}
		}

		setIngredients(newList);
	}

	public void setTemporaryEvaluation(Evaluation temporaryEvaluation) {
		this.temporaryEvaluation = temporaryEvaluation;
	}

	public Evaluation getTemporaryEvaluation() {
		return temporaryEvaluation;
	}

	public boolean hasTemporaryEvaluation() {
		return temporaryEvaluation != null;
	}

	public void clearTemporaryEvaluation() {
		temporaryEvaluation = null;
	}

	/**
	 * removes all ingredients in the list that are below the given threshold
	 * (in ounces)
	 * 
	 * @param threshold
	 * @return whether or not the list was actually changed
	 */
	public boolean removeIngedientsBelowThreshold(double threshold) {
		boolean madeChange = false;

		List<MeasuredIngredient> newList = new ArrayList<MeasuredIngredient>();

		for (MeasuredIngredient mi : getIngredients()) {
			if (mi.getAmountInOunces() >= threshold) {
				newList.add(mi);
			} else {
				madeChange = true;
			}
		}

		setIngredients(newList);

		return madeChange;
	}

	// *** If we ever have issues where an ingredient occurs more than twice
	// this should fix that problem
	/*
	 * public void combineSimilarIngredients() { for (int i = 0; i <
	 * ingredients.size(); i++) { MeasuredIngredient ingredient =
	 * ingredients.get(i);
	 * 
	 * Stack<Integer> removal = new Stack<Integer>(); for (int j = i + 1; j <
	 * ingredients.size(); j++) { MeasuredIngredient secondIngredient =
	 * ingredients.get(j); if
	 * (ingredient.getIngredient().equals(secondIngredient.getIngredient())) {
	 * removal.push(j); try {
	 * ingredient.setAmountInOunces(ingredient.getAmountInOunces() +
	 * secondIngredient.getAmountInOunces()); } catch (ItemNotFoundException e)
	 * { e.printStackTrace(); } } } while (!removal.isEmpty()) { int x =
	 * removal.pop(); ingredients.remove(x); } } }
	 */

	public GroupFormatRecipe getGroupFormat(GroupFormatType type) {
		return new GroupFormatRecipe(this, type);
	}

	public String toOnlineString() {
		String str = "";
		DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
		Date date = Calendar.getInstance().getTime();

		str += "\t<recipe>\n";
		str += "\t\t<name>" + getName() + "</name>\n";

		String dateTime = df.format(date);
		str += "\t\t<date>" + dateTime + "</date>\n";
		str += "\t\t<starEval>Not Available</starEval>\n";
		str += "\t\t<donutEval>Not Available</donutEval>\n";
		str += "\t\t<totalEval>"
				+ getTemporaryEvaluation().getNumericalEvaluation()
				+ "</totalEval>\n";
		str += "\t\t<ingredients>\n";

		for (MeasuredIngredient mi : getIngredientsOrderedByAmount()) {
			str += "\t\t\t"
					+ mi.getHumanReadableMeasuredIngredient().toOnlineString()
					+ "\n";
		}

		str += "\t\t</ingredients>\n\t</recipe>";

		return str;
	}
	
	public List<GroupReport> getGroupReports(GroupFormatType groupLevel) throws Exception {
		List<GroupReport> reports = new ArrayList<GroupReport>();
		Map<String, Double> totalAmounts = new HashMap<String, Double>();
		HashMap<String, Set<String>> ingredientSets = new HashMap<String, Set<String>>();

		List<IngredientGroup> superGroups = DbManager.getSingleton().getIngredientGroups().getChildrenGroups();
		List<IngredientGroup> groups = new ArrayList<IngredientGroup>();
		
		if (groupLevel == GroupFormatType.SUPER) {
			groups = superGroups;
		} else if (groupLevel == GroupFormatType.SUB) {
			for (IngredientGroup group : superGroups) {
				groups.addAll(group.getChildrenGroups());
			}
		} else {
			throw new Exception("Unsupported Group Format Type");
		}
		
		for (MeasuredIngredient measuredIngredient : ingredients) {
			double amount = measuredIngredient.getAmountInOunces();
			Ingredient ingredient = measuredIngredient.getIngredient();
			
			IngredientGroup group;
			
			if (groupLevel == GroupFormatType.SUPER) {
				group = ingredient.getParentGroup().getParentGroup();
			} else if (groupLevel == GroupFormatType.SUB) {
				group = ingredient.getParentGroup();
			} else {
				throw new Exception("Unsupported Group Format Type");
			}
			
			String groupName = group.getName();
			if (totalAmounts.containsKey(groupName)) {
				double current = totalAmounts.get(groupName);
				current += amount;
				totalAmounts.put(groupName, new Double(current));
				
				ingredientSets.get(groupName).add(ingredient.getName());
			} else {
				totalAmounts.put(groupName, amount);
				Set<String> ingredientSet = new HashSet<String>();
				ingredientSet.add(ingredient.getName());
				ingredientSets.put(groupName, ingredientSet);
			}
		}
		
		for (IngredientGroup group : groups) {
			GroupReport report;
			if (totalAmounts.containsKey(group.getName())) {
				double amount = totalAmounts.get(group.getName());
				int count = ingredientSets.get(group.getName()).size();
				report = new GroupReport(group,amount,count);
			} else {
				report = new GroupReport(group,0,0);
			}
			reports.add(report);
		}
		
		return reports;
	}
}
