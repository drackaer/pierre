/**
 * 
 */
package cooking.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import cooking.recipe.*;

/**
 * @author sburton
 *
 */
public class CsvController {
	/**
	 * Saves the list out to the give csv file
	 * 
	 * @param list The list to serialize
	 * @param csvFile The file to save
	 * @throws IOException
	 */
	public <T extends ICsvSerializable> void saveToCsv(List<T> list, String csvFile) throws IOException {
		FileWriter writer = new FileWriter(csvFile);
		
		for (ICsvSerializable item : list) {
			String data = item.convertToCsvFormat();
			writer.write(data + "\n");
		}
		
		writer.close();
	}
	
	/**
	 * Reads a list of ingredients from a csv file
	 * 
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public IngredientGroup loadIngredientsFromCsv(String csvFile) throws IOException {
		IngredientGroup allGroups = new IngredientGroup();
		allGroups.setWholeQuantity(4.0);
		
		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		
		String line;
		
		IngredientGroup tierOnePointer = allGroups;
		IngredientGroup tierTwoPointer = allGroups;
		
		while ((line = reader.readLine()) != null) {
			if(line.startsWith("**")){
				tierTwoPointer = new IngredientGroup();
				tierTwoPointer.setParentGroup(tierOnePointer);
				tierTwoPointer.loadFromCsvFormat(line.substring(2));
			}
			else if(line.startsWith("*")){
				tierOnePointer = new IngredientGroup();
				tierOnePointer.setParentGroup(allGroups);
				tierOnePointer.loadFromCsvFormat(line.substring(1));
			}
			else{
				Ingredient ingredient = new Ingredient();
				ingredient.setParentGroup(tierTwoPointer);
				ingredient.loadFromCsvFormat(line);
			}
		}

		reader.close();
		
		return allGroups;
	}

	/**
	 * Reads a list of measurements from a csv file
	 * 
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public List<Measurement> loadMeasurementsFromCsv(String csvFile) throws IOException {
		List<Measurement> list = new ArrayList<Measurement>();

		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		
		String line;
		
		while ((line = reader.readLine()) != null) {
			Measurement measurement = new Measurement();
			measurement.loadFromCsvFormat(line);
			list.add(measurement);
		}

		reader.close();
		
		return list;
	}

	/**
	 * Reads a list of recipes from a csv file
	 * 
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public List<Recipe> loadRecipesFromCsv(String csvFile) throws IOException {
		List<Recipe> list = new ArrayList<Recipe>();
		
		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		
		String line;
		boolean inRecipe = false;
		String currentRecipe = "";
		
		while ((line = reader.readLine()) != null) {
			if (line.equals(Recipe.CSV_HEADER.trim())) {
				
				if (inRecipe) {
					if (!currentRecipe.equals("")) {
						Recipe recipe = new Recipe();
						recipe.loadFromCsvFormat(currentRecipe);
						list.add(recipe);
					}
					
					currentRecipe = "";
				}
				inRecipe = true;
			}
			
			currentRecipe += line + "\n";
		}
		
		if (!currentRecipe.equals("")) {
			Recipe recipe = new Recipe();
			recipe.loadFromCsvFormat(currentRecipe);
			list.add(recipe);
		}
		
		reader.close();
		
		return list;
	}

	public List<Rating> loadRatingsFromCsv(String ratingsFile) throws IOException {
		List<Rating> list = new ArrayList<Rating>();
		
		BufferedReader reader = new BufferedReader(new FileReader(ratingsFile));
		
		String line;
				
		while ((line = reader.readLine()) != null) {
			Rating rating = new Rating("");
			rating.loadFromCsvFormat(line);
			list.add(rating);
		}

		reader.close();
		
		return list;
	}
	
}
