package cooking.crawler;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cooking.database.InspiringSetType;
import cooking.database.DbManager;
import cooking.recipe.Recipe;

public class Crawler {
	
	//private static String ALL_RECIPES_ADDRESS = "http://allrecipes.com/Recipes/Soups-Stews-and-Chili/ViewAll.aspx";
	private static String ALL_RECIPES_ADDRESS = "http://allrecipes.com/Recipes/Soups-Stews-and-Chili/Chili/ViewAll.aspx";
	private static String FOOD_NETWORK_ADDRESS_CHILI = "http://www.foodnetwork.com/search/delegate.do?Nr=Record%20Type:Result&N=4294961271+501";
	private static String FOOD_NETWORK_ADDRESS_SOUP =  "http://www.foodnetwork.com/search/delegate.do?Nr=Record%20Type:Result&N=4294961231+501";
	
	/**
	 * @param args
	 * This is a Crawler
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Crawler crawler = new Crawler();
		List<Recipe> recipes = new ArrayList<Recipe>();

		DbManager manager = DbManager.getSingleton();
		manager.loadData(InspiringSetType.FULL, true);
		manager.clearRecipes();
		
		recipes.addAll(crawler.getRecipesFromAllRecipes());
		recipes.addAll(crawler.getRecipesFromFoodNetwork());
		
		for (Recipe recipe : recipes) {
			if (recipe.getIngredients().size() > 0)
				manager.addRecipe(recipe);
		}

		try {
			//manager.saveDataToARFF();
			//manager.saveDataToAbbreviatedARFF();
			manager.saveChanges(false);
			String outputFile = "data/trainingrecipes_scaledTo100.csv";
			manager.saveScaledRecipes(outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<Recipe> getRecipesFromFoodNetwork() throws IOException {
		System.out.println("Beginning Food Network Chili");
		int numSearchPages = getNumberOfPagesForFoodNetwork(FOOD_NETWORK_ADDRESS_CHILI);
		RecipePageParser parser = new RecipePageParser();

		List<Recipe> recipes = new ArrayList<Recipe>();
		
		for (int i = 0; i < numSearchPages; i++) {
			List<String> recipePages = getRecipePagesForFoodNetwork(FOOD_NETWORK_ADDRESS_CHILI + "&No=" + (i*12));
			for (String recipePage : recipePages) {
				recipes.add(parser.getRecipeFromPageForFoodNetwork(recipePage));
			}
			System.out.println("Parsed FN chili page " + (i + 1) + " of " + numSearchPages);
		}
		
		System.out.println("Beginning Food Network Soup");
		//numSearchPages = getNumberOfPagesForFoodNetwork(FOOD_NETWORK_ADDRESS_SOUP);
		numSearchPages = 0;
		for (int i = 0; i < numSearchPages; i++) {
			List<String> recipePages = getRecipePagesForFoodNetwork(FOOD_NETWORK_ADDRESS_SOUP + "&No=" + (i*12));
			for (String recipePage : recipePages) {
				recipes.add(parser.getRecipeFromPageForFoodNetwork(recipePage));
			}
			System.out.println("Parsed FN soup page " + (i + 1) + " of " + numSearchPages);
		}
		
		System.out.println("Done with Food Network");
		return recipes;
	}

	private List<String> getRecipePagesForFoodNetwork(String url) {
		Set<String> urls = new HashSet<String>();
		List<String> output = new ArrayList<String>();
		
		try {
			URL recipeUrl = new URL(url);
			InputStream strm = recipeUrl.openStream();
			BufferedReader read = new BufferedReader(new InputStreamReader(strm));
			String s;
			
			while ((s = read.readLine()) != null)
			{
				if (s.contains("href=\"/recipes/") && !s.contains("class")) {
					urls.add(parseLinkLineForFoodNetwork(s.trim()));
				}
			}
			output.addAll(urls);
		}
		catch (IOException e) {
			System.out.println("Error connecting to page: " + url);
		}
		
		return output;
	}

	private String parseLinkLineForFoodNetwork(String line) {
		String url = "";
		
		String[] splitStrings = line.split("\"");
		
		for (String currentString : splitStrings) {
			if (currentString.contains("index.html")) {
				url = currentString;
			}
		}
		return "http://www.foodnetwork.com" + url;
	}

	private int getNumberOfPagesForFoodNetwork(String recipeAddress) throws IOException {
		int numPages = 1;
		
		URL recipeUrl = new URL(recipeAddress);
		InputStream strm = recipeUrl.openStream();
		BufferedReader read = new BufferedReader(new InputStreamReader(strm));
		String s;
		
		while ((s = read.readLine()) != null)
		{
			if (s.contains("Showing")) {
				numPages = parseNumberOfPagesLineForFoodNetwork(s);
			}
		}
		
		return numPages;
	}

	private int parseNumberOfPagesLineForFoodNetwork(String s) {
		int numPages = 0;
		int numRecipes = 0;
		String[] lines = s.trim().split("\\s");
		
		for (String line : lines) {
			if (line.contains("</p>")) {
				numRecipes = Integer.parseInt(line.replaceAll("</p>", ""));
				break;
			}
		}
		
		numPages = (int) Math.ceil((numRecipes / 12.0));

		return numPages;
	}

	private List<Recipe> getRecipesFromAllRecipes() throws IOException {
		System.out.println("Starting All Recipes");
		int numSearchPages = getNumberOfPagesForAllRecipes(ALL_RECIPES_ADDRESS);
		RecipePageParser parser = new RecipePageParser();

		List<Recipe> recipes = new ArrayList<Recipe>();
		
		for (int i = 1; i <= numSearchPages; i++) {
			List<String> recipePages = getRecipePagesForAllRecipes(ALL_RECIPES_ADDRESS + "?Page=" + i);
			for (String recipePage : recipePages) {
				recipes.add(parser.getRecipeFromPageForAllRecipes(recipePage));
			}
			System.out.println("Parsed AR page " + i + " of " + numSearchPages);
		}
		
		System.out.println("Done with All Recipes");
		return recipes;
	}
	
	private int getNumberOfPagesForAllRecipes(String recipeAddress) throws IOException {
		int numPages = 1;
		
		URL recipeUrl = new URL(recipeAddress);
		InputStream strm = recipeUrl.openStream();
		BufferedReader read = new BufferedReader(new InputStreamReader(strm));
		String s;
		
		while ((s = read.readLine()) != null)
		{
			if (s.contains("Displaying")) {
				numPages = parseNumberOfPagesLineForAllRecipes(s);
			}
		}
		
		return numPages;
	}

	private int parseNumberOfPagesLineForAllRecipes(String s) {
		int numPages = 0;
		int numRecipes = 0;
		String[] lines = s.trim().split("\\s");
		
		for (String line : lines) {
			if (line.contains(")")) {
				String newLine = line.replaceAll("\\)", "");
				numRecipes = Integer.parseInt(newLine.replaceAll(",", ""));
				break;
			}
		}
		
		numPages = (int) Math.ceil((numRecipes / 20.0));

		return numPages;
	}

	// Returns a list of recipe page URLs
	public List<String> getRecipePagesForAllRecipes(String url) throws IOException {
		List<String> urls = new ArrayList<String>();
		
		URL recipeUrl = new URL(url);
		InputStream strm = recipeUrl.openStream();
		BufferedReader read = new BufferedReader(new InputStreamReader(strm));
		String s;
		
		while ((s = read.readLine()) != null)
		{
			if (s.contains("lnkRecipeTitle")) {
				urls.add(parseLinkLineForAllRecipes(s.trim()));
			}
		}
		return urls;
	}

	private String parseLinkLineForAllRecipes(String line) {
		String url = "";
		
		String[] splitStrings = line.split("\"");
		
		for (String currentString : splitStrings) {
			if (currentString.contains("http://allrecipes.com")) {
				url = currentString;
			}
		}
		return url;
	}
	
}
