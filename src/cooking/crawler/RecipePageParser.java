package cooking.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cooking.database.ItemNotFoundException;
import cooking.recipe.Rating;
import cooking.recipe.Recipe;

// Parses a recipe page from AllRecipes.com
// Get rating
// Get name
// Get Ingredient lines and pass to measured ingredients
// Generate ID somehow?
public class RecipePageParser {
	
	public Recipe getRecipeFromPageForAllRecipes(String pageAddress) throws IOException {
		URL recipeUrl = new URL(pageAddress);
		InputStream strm = recipeUrl.openStream();
		BufferedReader recipeReader = new BufferedReader(new InputStreamReader(strm));
		String s;
		List<String> ingredients = new ArrayList<String>();
		double numericalRating = -1;
		int numberOfRatings = 0;
		int numberOfTimesSaved = 0;
		String name = "";
		
		// Generate ID?
		boolean isParsingIngredients = false;
		boolean isParsingTitle = false;
		
		while ((s = recipeReader.readLine()) != null)
		{
			if (s.contains("\"This Kitchen Approved Recipe has an average star rating")) {
				numericalRating = ParseRatingLineForAllRecipes(s);
				continue;
			}
			
			if (s.contains("\"count\"")) {
				numberOfRatings = ParseCountLineForAllRecipes(s);
				continue;
			}
			
			if (s.contains("class=\"unsavedExempt userSaved\"")) {
				numberOfTimesSaved = ParseSavedLineForAllRecipes(s);
				continue;
			}
			
			if (s.contains("<div class=\"ingredients\" style=\"margin-top: 10px;\">")) {
				// Start Parsing Ingredients
				isParsingIngredients = true;
				continue;
			}
			
			if (isParsingTitle) {
				// Parse title
				name = s.trim();
				isParsingTitle = false;
				continue;
			}
			
			// Is next line the title?
			if (s.contains("<title>")) {
				isParsingTitle = true;
				continue;
			}
			
			// Is this an ingredient line?
			if (isParsingIngredients && s.contains("</li>")) {
				s = s.replaceAll("</li>", "").replaceAll("</a>", "").replaceAll("<.+>", "").trim();
				if (!isWeirdIngredientForAllRecipes(s)) {
					ingredients.add(s);
				}
				continue;
			}
			
			if (s.contains("</div>") && isParsingIngredients) {
				// Done parsing ingredients
				isParsingIngredients = false;
				continue;
			}
		}
		Rating rating = new Rating(name, numericalRating, numberOfRatings, numberOfTimesSaved);
		Recipe recipe = new Recipe(name, ingredients, rating);
		try {
			recipe.combineSameIngredients();
		} catch (ItemNotFoundException e) {
			// This shouldn't ever happen...
			e.printStackTrace();
		}
		
		return recipe;
	}

	private boolean isWeirdIngredientForAllRecipes(String s) {
		boolean retval = false;
		
		if (s.equals("&nbsp;") || s.contains(":")) {
			retval = true;
		}
		
		return retval;
	}

	private int ParseSavedLineForAllRecipes(String s) {
		int output = 0;
		s = s.trim();
		s = s.replaceAll(",", "");
		String[] lines = s.split("[>\\s]");
		for (String line : lines) {
			if (line.matches("\\d+")) {
				output = Integer.parseInt(line);
				break;
			}
		}
		return output;
	}

	private int ParseCountLineForAllRecipes(String s) {
		int output = 0;
		s = s.trim();
		s = s.replaceAll(",", "");
		String[] lines = s.split("[><]");
		for (String line : lines) {
			if (line.matches("\\d+")) {
				output = Integer.parseInt(line);
				break;
			}
		}
		return output;
	}

	private double ParseRatingLineForAllRecipes(String s) {
		double output = 0;
		s = s.trim();
		String[] lines = s.split("\\s");
		for (String line : lines) {
			if (line.matches("\\d\\.\\d\"") || line.matches("\\d\\.\\d\\d\"")) {
				line = line.replaceAll("\"", "");
				output = Double.parseDouble(line);
				break;
			}
		}
		return output;
	}

	public Recipe getRecipeFromPageForFoodNetwork(String pageAddress) throws IOException {
		URL recipeUrl = new URL(pageAddress);
		InputStream strm = recipeUrl.openStream();
		BufferedReader recipeReader = new BufferedReader(new InputStreamReader(strm));
		String s;
		List<String> ingredients = new ArrayList<String>();
		double numericalRating = -1;
		int numberOfRatings = 0;
		int numberOfTimesSaved = 0;
		String name = "";
		
		// Generate ID?
		
		while ((s = recipeReader.readLine()) != null)
		{
			if (s.contains("\"count\"")) {
				numberOfRatings = ParseCountLineForFoodNetwork(s.trim());
				numericalRating = ParseRatingLineForFoodNetwork(s.trim());
				continue;
			}
			
			if (s.contains("<title>")) {
				name = getTitleForFoodNetwork(s.trim());
				continue;
			}
			
			// Is this an ingredient line?
			if (s.contains("<li class=\"ingredient\">")) {
				s = s.replaceAll("<[\\w\\s\\d:=\"/\\.-]+>", "").trim();
				//System.out.println(s);
				if (!isWeirdIngredientForFoodNetwork(s)) {
					ingredients.add(s);
				}
				continue;
			}
		}
		Rating rating = new Rating(name, numericalRating, numberOfRatings, numberOfTimesSaved);
		Recipe recipe = new Recipe(name, ingredients, rating);
		try {
			recipe.combineSameIngredients();
		} catch (ItemNotFoundException e) {
			// This shouldn't ever happen...
			e.printStackTrace();
		}
		return recipe;
	}

	private boolean isWeirdIngredientForFoodNetwork(String s) {
		boolean retval = false;
		String lower = s.toLowerCase();
		if (lower.contains("special equipment:")
				|| lower.contains("copyright")
				|| lower.contains("preheat")
				|| lower.contains("recipe courtesy")) {
			retval = true;
		}
		
		return retval;
	}

	private double ParseRatingLineForFoodNetwork(String s) {
		double rating = 0;
		String[] lines = s.split("\"");
		for (String line : lines) {
			if (line.matches("\\d")) {
				rating = Double.parseDouble(line);
				break;
			}
		}
		return rating;
	}

	private int ParseCountLineForFoodNetwork(String s) {
		int count = 0;
		String[] lines = s.split("[<>]");
		for (String line : lines) {
			line.replaceAll(",", "");
			if (line.matches("\\d+")) {
				count = Integer.parseInt(line);
				break;
			}
		}
		return count;
	}

	private String getTitleForFoodNetwork(String s) {
		String name = "";
		name = s.replaceAll("</?title>", "");
		return name;
	}
}
