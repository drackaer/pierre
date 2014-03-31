package cooking.database;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cooking.recipe.*;
import cooking.recipe.stats.Aggregator;

public class DbManager {
	private DbManager() {
		ingredients = new ArrayList<Ingredient>();
		measurements = new ArrayList<Measurement>();
		recipes = new ArrayList<Recipe>();
	}
	
	public Logger logger = Logger.getLogger(this.getClass());
	
	// TODO: push these filenames into a config file
	public String csvFileIngredients = "data/ingredients_newFormat.csv";
	public String csvFileMeasurements = "data/measurements.csv";
	
	// Full Set
	public String csvFileRecipes = "data/recipes.csv";
	public String csvFileRatings = "data/ratings.csv";
	
	// Chili Set
	//public String csvFileRecipes = "data/trainingrecipes.csv";
	//public String csvFileRatings = "data/trainingratings.csv";
	
	
	public String csvFileRandomRecipes = "data/randomrecipes.csv";
	
	// private storage
	private static double SCALE_SIZE = 100.0;
	private List<Ingredient> ingredients;
	private IngredientGroup groupOfAllIngredientGroups;
	private List<Measurement> measurements;
	private List<Recipe> recipes;
	private List<Recipe> randomRecipes;
	
	public void clearRecipes() {
		recipes = new ArrayList<Recipe>();
	}
	
	public void clearRandomRecipes() {
		randomRecipes = new ArrayList<Recipe>();
	}
	
	public IngredientGroup getIngredientGroups(){
		return groupOfAllIngredientGroups;
	}
	
	public List<Ingredient> getAllIngredients() {
		return ingredients;
	}
	
	public List<Measurement> getAllMeasurements() {
		return measurements;
	}
	
	public List<Recipe> getAllRecipes() {
		return recipes;
	}
	
	public List<Recipe> getAllRecipesWithRandom() {
		List<Recipe> recipes = new ArrayList<Recipe>();
		recipes.addAll(this.recipes);
		recipes.addAll(this.randomRecipes);
		return recipes;
	}
	
	public List<Recipe> getRandomRecipes() {
		return randomRecipes;
	}
	
	public List<Rating> getAllRatings() {
		List<Rating> ratings = new ArrayList<Rating>();
		
		for (Recipe recipe : recipes) {
			ratings.add(recipe.getRating());
		}
		
		return ratings;
	}
	
	public List<Recipe> getRandomSampleOfRecipes(int maxSampleSize) {

		List<Recipe> list;
		List<Recipe> allRecipes = getAllRecipes();

		if (maxSampleSize >= allRecipes.size()) {
			list = allRecipes;
		}
		else {
			list = new ArrayList<Recipe>();
			
			Random random = new Random();
			for (int i = 0; i < maxSampleSize; i++) {
				int randomIndex = random.nextInt(allRecipes.size());
				
				list.add(allRecipes.get(randomIndex));
			}
		}
		
		
		return list;
	}
	
	public Ingredient findIngredientByName(String name) throws ItemNotFoundException {
		Ingredient ingredient = null;
		
		for (Ingredient cur : getAllIngredients()) {
			if (cur.getName().equals(name)) {
				ingredient = cur;
				break;
			}
		}
		
		if (ingredient == null) {
			throw new ItemNotFoundException();
		}

		return ingredient;
	}
	
	public Ingredient findIngredientByNameOrAnyVariant(String name) throws ItemNotFoundException {
		Ingredient ingredient = null;
		
		for (Ingredient cur : getAllIngredients()) {
			if (cur.matchesAnyVariant(name)) {
				ingredient = cur;
				break;
			}
		}
		
		if (ingredient == null) {
			throw new ItemNotFoundException();
		}

		return ingredient;
	}	
	
	public Measurement findMeasurementByName(String name) throws ItemNotFoundException {
		Measurement measurement = null;
		
		for (Measurement cur : getAllMeasurements()) {
			if (cur.getName().equals(name)) {
				measurement = cur;
				break;
			}
		}
		
		if (measurement == null) {
			throw new ItemNotFoundException();
		}

		return measurement;
	}
	
	public Recipe findRecipeByName(String name) throws ItemNotFoundException {
		Recipe recipe = null;
		
		for (Recipe cur : getAllRecipes()) {
			if (cur.getName().equals(name)) {
				recipe = cur;
				break;
			}
		}
		
		if (recipe == null) {
			throw new ItemNotFoundException();
		}

		return recipe;
	}
	
	// Returns the parent group that contains the given ingredient.
	// Assumes ingredient can only be in one group.
	// Assumes there are only two tiers of ingredient groups.
	public IngredientGroup findIngredientGroupByIngredient(Ingredient ingredient) throws ItemNotFoundException {
		IngredientGroup group = null;
		
		for (IngredientGroup cur : groupOfAllIngredientGroups.getChildrenGroups()) {
			if (cur.containsIngredient(ingredient)) {
				group = cur;
				break;
			}
		}
		
		if (group == null) {
			throw new ItemNotFoundException();
		}
		return group;
	}

	public IngredientGroup findIngredientGroupByName(String name) throws ItemNotFoundException {
		IngredientGroup group = null;
		
		for (IngredientGroup cur : groupOfAllIngredientGroups.getChildrenGroups()) {
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
	
	// The following methods are used to add data to the stored information.
	// note that this data will not be persisted until some type of "save" is called
	/**
	 * Adds an ingredient to the DB, note that it is not persisted until saveChanges is called.
	 * @param ingredient
	 */
	public void addIngredient(Ingredient ingredient){
		ingredients.add(ingredient);
	}
	
	/**
	 * Adds a measurement to the DB, note that it is not persisted until saveChanges is called.
	 * @param measurement
	 */
	public void addMeasurement(Measurement measurement) {
		measurements.add(measurement);
	}
	
	/**
	 * Adds a recipe to the DB, note that it is not persisted until saveChanges is called.
	 * @param recipe
	 */
	public void addRecipe(Recipe recipe) {
		recipes.add(recipe);
	}
	
	/**
	 * Adds a random recipe to the DB, note that it is not persisted until saveChanges is called.
	 * @param recipe
	 */
	public void addRandomRecipe(Recipe recipe) {
		randomRecipes.add(recipe);
	}
	
	/**
	 * Persists any changes to the DB.  This will throw an exception if called for the test data "db"
	 * @throws Exception 
	 */
	public void saveChanges(boolean updateFullDatabase) throws Exception {
		saveDataToCsv(updateFullDatabase);
	}
	
	public void loadData(InspiringSetType inspiringSetType, boolean hierarchy) throws IOException {
		Aggregator.clearIngredientStatsMap();
		
		if (hierarchy) {
			csvFileIngredients = "data/ingredients_newFormat.csv";
		} else {
			csvFileIngredients = "data/ingredients_degeneratehierarchy.csv";
		}
		
		switch (inspiringSetType) {
		case FULL:
			csvFileRecipes = "data/recipes.csv";
			csvFileRatings = "data/ratings.csv";
			csvFileMeasurements = "data/measurements.csv";
			break;
		case CHILI:
			csvFileMeasurements = "data/measurements.csv";
			csvFileRecipes = "data/trainingrecipes.csv";
			csvFileRatings = "data/trainingratings.csv";
			break;
		case TEST:
			csvFileIngredients = "data/testCaseIngredients.csv";
			csvFileMeasurements = "data/testCaseMeasurements.csv";
			csvFileRecipes = "data/testCaseRecipes.csv";
			csvFileRatings = "data/testCaseRatings.csv";
			break;
		}
		loadDataFromCsv();
	}
	
	public void loadDataFromTestCaseCsvFiles() throws IOException {
		loadData(InspiringSetType.TEST, true);
	}
	
	/**
	 * Loads up all the data from csv and stores in memory
	 * 
	 * @throws IOException
	 */
	private void loadDataFromCsv() throws IOException {
		logger.info("Loading data from CSV file");
		
		CsvController controller = new CsvController();

		groupOfAllIngredientGroups = controller.loadIngredientsFromCsv(csvFileIngredients);
		ingredients = groupOfAllIngredientGroups.getAllIngredients();
		measurements = controller.loadMeasurementsFromCsv(csvFileMeasurements);
		try {
			recipes = controller.loadRecipesFromCsv(csvFileRecipes);
			List<Rating> ratings = controller.loadRatingsFromCsv(csvFileRatings);
			matchRatingsToRecipes(ratings);
		} catch (FileNotFoundException exc) {
			logger.debug("Unable to find recipe file.  Creating new Recipe Database.");
		}
		/*for(IngredientGroup group:groupOfAllIngredientGroups.getChildrenGroups())
			System.out.println(group.getName());*/
		
	}
	
	public void loadRandomRecipes() throws IOException {
		logger.info("Loading Random data from CSV file");
		
		CsvController controller = new CsvController();

		try {
			randomRecipes = controller.loadRecipesFromCsv(csvFileRandomRecipes);
		} catch (FileNotFoundException exc) {
			logger.debug("Unable to find random recipe file.");
		}
	}
	
	private void matchRatingsToRecipes(List<Rating> ratings) {
		for (Rating rating : ratings) {
			String recipeName = rating.getRecipeName();
			try {
				Recipe recipe = findRecipeByName(recipeName);
				recipe.setRating(rating);
			} catch (ItemNotFoundException e) {
				logger.error("Could Not Find Recipe with name " + recipeName);
			}
		}
		
	}

	public void saveRandomRecipes() throws IOException {
		CsvController controller = new CsvController();
		controller.saveToCsv(randomRecipes, csvFileRandomRecipes);
	}
	
	private void saveDataToCsv(boolean updateFullDatabase) throws IOException {
		CsvController controller = new CsvController();

		if (updateFullDatabase) {
			controller.saveToCsv(ingredients, csvFileIngredients);
			controller.saveToCsv(measurements, csvFileMeasurements);
		}
		controller.saveToCsv(recipes, csvFileRecipes);
		controller.saveToCsv(getAllRatings(), csvFileRatings);
	}
	
	/**
	 * Creates a scaled version of each recipe and saves that list to the given file.
	 * 
	 * @param totalVolumePerRecipe
	 * @param outputFile
	 * @throws IOException - if the output file cannot be opened
	 * @throws ItemNotFoundException - if the "ounces" measurement cannot be found
	 */
	public void saveScaledRecipes(String outputFile) throws IOException, ItemNotFoundException {
		CsvController controller = new CsvController();
		
		List<Recipe> scaledRecipes = new ArrayList<Recipe>();

		for (Recipe originalRecipe : getAllRecipes()) {
			// HAHA, "original recipe" maybe I should name the new one "extra crispy"...
			
			Recipe scaledRecipe = originalRecipe.getScaledVersion(getScaleSize());
			scaledRecipes.add(scaledRecipe);
		}
		
		controller.saveToCsv(scaledRecipes, outputFile);
	}
	
	public static final String ARFF_LOCATION = "recipes.arff";
	// We might want to look into adding features or something later on, but this is really simple now.
	public void saveDataToARFF() throws IOException {
		StringBuffer output = new StringBuffer();
		
		output.append("@RELATION recipes\n\n");
		
		for (Ingredient ing : ingredients) {
			output.append("@ATTRIBUTE " + ing.toString().replaceAll("\\s", "_") + " REAL\n");
		}
		output.append("@ATTRIBUTE rating REAL\n");
		output.append("@DATA\n");
		
		for (Recipe recipe : recipes) {
			if (recipe.getRating().getNumericalRating() <= 0) {
				continue;
			}
			double[] attributeVector = new double[ingredients.size()];
			
			for (int i = 0; i < attributeVector.length; i++) {
				attributeVector[i] = 0;
			}
			
			for (MeasuredIngredient mi : recipe.getIngredients()) {
				int index = ingredients.indexOf(mi.getIngredient());
				
				if (index == -1) {
					logger.error("***MAJOR ERROR: Ingredient index not found.***");
				}
				
				attributeVector[index] = mi.getAmountInOunces();
			}
			
			for (double entry : attributeVector) {
				output.append(entry + ",");
			}
			output.append(recipe.getRating().getNumericalRating() + "\n");
		}
		
		File arffFile = new File(ARFF_LOCATION);
		FileWriter writer = new FileWriter(arffFile);
		
		writer.write(output.toString());
		writer.close();
	}
	
	// Reduces dimensionality of the ARFF based on the groupings.
	public void saveDataToAbbreviatedARFF() throws IOException, ItemNotFoundException {
		StringBuffer output = new StringBuffer();
		
		output.append("@RELATION recipes\n\n");
		
		List<IngredientGroup> parentGroups = groupOfAllIngredientGroups.getChildrenGroups();
		List<IngredientGroup> childGroups = new ArrayList<IngredientGroup>();
		
		for (IngredientGroup group : parentGroups) {
			childGroups.addAll(group.getChildrenGroups());
		}
		
		for (IngredientGroup group : childGroups) {
			output.append("@ATTRIBUTE " + group.getName().replaceAll("\\s", "_") + " REAL\n");
		}

		output.append("@ATTRIBUTE rating REAL\n");
		output.append("@DATA\n");
		
		for (Recipe recipe : recipes) {
			if (recipe.getRating().getNumericalRating() <= 0) {
				continue;
			}
			
			double[] attributeVector = convertRecipeToSubGroupFormat(recipe);
			
			for (int i = 0; i < attributeVector.length - 1; i++) {
				output.append(attributeVector[i] + ",");
			}
			output.append(recipe.getRating().getNumericalRating() + "\n");
		}
		/*
		List<IngredientGroup> groups = groupOfAllIngredientGroups.getChildrenGroups();
		
		for (IngredientGroup group : groups) {
			output.append("@ATTRIBUTE " + group.getName().replaceAll("\\s", "_") + " REAL\n");
		}

		output.append("@ATTRIBUTE rating REAL\n");
		output.append("@DATA\n");
		
		for (Recipe recipe : recipes) {
			if (recipe.getRating().getNumericalRating() <= 0) {
				continue;
			}
			
			double[] attributeVector = convertRecipeToGroupFormat(recipe);
			
			for (double entry : attributeVector) {
				output.append(entry + ",");
			}
			output.append(recipe.getRating().getNumericalRating() + "\n");
		}*/
		
		File arffFile = new File("Abbreviated" + ARFF_LOCATION);
		FileWriter writer = new FileWriter(arffFile);
		
		writer.write(output.toString());
		writer.close();
	}
	
	public double[] convertRecipeToSubGroupFormat(Recipe recipe) throws ItemNotFoundException {
		List<IngredientGroup> parentGroups = groupOfAllIngredientGroups.getChildrenGroups();
		List<IngredientGroup> childGroups = new ArrayList<IngredientGroup>();
		
		for (IngredientGroup group : parentGroups) {
			childGroups.addAll(group.getChildrenGroups());
		}
		
		double[] groupFormat = new double[childGroups.size() + 1];
		
		for (int i = 0; i < groupFormat.length; i++) {
			groupFormat[i] = 0;
		}
		
		for (MeasuredIngredient mi : recipe.getIngredients()) {
			int index = childGroups.indexOf(mi.getIngredient().getParentGroup());
			
			if (index == -1) {
				logger.error("***MAJOR ERROR: Ingredient Group index not found.***");
			}
			
			groupFormat[index] += mi.getAmountInOunces();
		}
		groupFormat[groupFormat.length - 1] = recipe.getRating().getNumericalRating();
		
		return groupFormat;
	}
	
	private static DbManager singleton = null;
	/**
	 * Gets a static/singleton DbManager.  Note that because this is a static instance it is not threadsafe.
	 * 
	 * @return
	 */
	public static DbManager getSingleton() {
		if (singleton == null) {
			singleton = new DbManager();
		}
		
		return singleton;
	}
	
	public static void clearSingleton() {
		singleton = null;
	}

	public ArrayList<ArrayList<Double>> getSubGroupLearnerData() {
		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
		for (Recipe recipe : recipes) {
			if (recipe.getRating().getNumericalRating() <= 0) continue;
			
			ArrayList<Double> datum = new ArrayList<Double>();
			double[] d;
			try {
				d = convertRecipeToSubGroupFormat(recipe);
				for (int i = 0; i < d.length; i++) {
					datum.add(d[i]);
				}
				
				data.add(datum);
			} catch (ItemNotFoundException e) {
				e.printStackTrace();
			}
		}
		for (Recipe recipe : randomRecipes) {
			ArrayList<Double> datum = new ArrayList<Double>();
			double[] d;
			try {
				d = convertRecipeToSubGroupFormat(recipe);
				for (int i = 0; i < d.length-1; i++) {
					datum.add(d[i]);
				}
				datum.add(0.01);
				data.add(datum);
			} catch (ItemNotFoundException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public ArrayList<ArrayList<Double>> getSuperGroupLearnerData() {
		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
		for (Recipe recipe : recipes) {
			if (recipe.getRating().getNumericalRating() <= 0) continue;
			
			ArrayList<Double> datum = new ArrayList<Double>();
			double[] d;
			try {
				d = convertRecipeToSuperGroupFormat(recipe);
				for (int i = 0; i < d.length; i++) {
					datum.add(d[i]);
				}
				
				data.add(datum);
			} catch (ItemNotFoundException e) {
				e.printStackTrace();
			}
		}
		for (Recipe recipe : randomRecipes) {
			ArrayList<Double> datum = new ArrayList<Double>();
			double[] d;
			try {
				d = convertRecipeToSuperGroupFormat(recipe);
				for (int i = 0; i < d.length-1; i++) {
					datum.add(d[i]);
				}
				datum.add(0.01);
				data.add(datum);
			} catch (ItemNotFoundException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public double[] convertRecipeToSuperGroupFormat(Recipe recipe) throws ItemNotFoundException {
		List<IngredientGroup> groups = groupOfAllIngredientGroups.getChildrenGroups();
		
		double[] groupFormat = new double[groups.size() + 1];
		
		for (int i = 0; i < groupFormat.length; i++) {
			groupFormat[i] = 0;
		}
		
		for (MeasuredIngredient mi : recipe.getIngredients()) {
			int index = groups.indexOf(findIngredientGroupByIngredient(mi.getIngredient()));
			
			if (index == -1) {
				logger.error("***MAJOR ERROR: Ingredient Group index not found.***");
			}
			
			groupFormat[index] += mi.getAmountInOunces();
		}
		
		groupFormat[groupFormat.length - 1] = recipe.getRating().getNumericalRating();
		
		return groupFormat;
	}

	public static double getScaleSize() {
		return SCALE_SIZE;
	}
	
	/**
	 * Prune all ingredients that do not exist in the recipe database.
	 * @return
	 */
	public int pruneIngredients() {
		int numPruned = 0;
		
		List<Ingredient> ingredientsCopy = new ArrayList<Ingredient>(ingredients);
		
		for (Ingredient ingredient : ingredientsCopy) {
			if (!Aggregator.containsIngredient(ingredient)) {
				ingredients.remove(ingredient);
				numPruned++;
			}
		}
		
		return numPruned;
	}
}
