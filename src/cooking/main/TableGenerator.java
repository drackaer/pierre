package cooking.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cooking.database.CsvController;
import cooking.database.InspiringSetType;
import cooking.database.DbManager;
import cooking.database.GroupReport;
import cooking.database.IngredientGroup;
import cooking.recipe.GroupFormatType;
import cooking.recipe.Recipe;
import cooking.recipe.stats.RecipeNGramCounter;

public class TableGenerator {
	private static int rareThreshold = 1;
	private static int highestNGram = 5;
	private static int lowestNGram = 1;
	
	public static void main(String[] args) throws Exception {
		DbManager man = DbManager.getSingleton();
		man.loadData(InspiringSetType.FULL, true);
		RecipeNGramCounter count = RecipeNGramCounter.getInstance();
		//String csvInputFile = "results/2011-7-5_chilieval_chilistats";
		String date = "2011-12-7";
		//String type = "chilieval_fullstats";
		//String type = "fulleval_fullstats";
		//String type = "fulleval_chilistats";
		//String type = "chilieval_chilistats";
		String type = "fullinspiring_chilieval_withdist";
	
		String csvInputFile = "results/2011-10-14_" + type;
		//String csvInputFile = "data/trainingrecipes.csv";
		//String csvInputFile = "results/2011-7-5_fulleval_chilistats";
		//String csvInputFile = "results/2011-7-5_fulleval_fullstats";
		
		HashMap<Integer, List<Integer>> strangeIngredientCounts = new HashMap<Integer, List<Integer>>();
		HashMap<Integer, List<Integer>> possibleIngredientCounts = new HashMap<Integer, List<Integer>>();
		HashMap<Integer, Integer> strangeIngredientSums = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> possibleIngredientSums = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> strangeIngredientMaxs = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> possibleIngredientMaxs = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> strangeIngredientMins = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> possibleIngredientMins = new HashMap<Integer, Integer>();
		HashMap<Integer, Double> strangeIngredientMeans = new HashMap<Integer, Double>();
		HashMap<Integer, Double> possibleIngredientMeans = new HashMap<Integer, Double>();
		HashMap<Integer, Double> strangeIngredientMedians = new HashMap<Integer, Double>();
		HashMap<Integer, Double> possibleIngredientMedians = new HashMap<Integer, Double>();
		HashMap<Integer, Double> strangeIngredientVariances = new HashMap<Integer, Double>();
		HashMap<Integer, Double> possibleIngredientVariances = new HashMap<Integer, Double>();
		for (int i = lowestNGram; i <= highestNGram; i++) {
			count.countNGrams(man.getAllRecipes(), i);
			strangeIngredientCounts.put(i, new ArrayList<Integer>());
			possibleIngredientCounts.put(i, new ArrayList<Integer>());
			strangeIngredientSums.put(i, 0);
			possibleIngredientSums.put(i, 0);
			strangeIngredientMaxs.put(i, Integer.MIN_VALUE);
			possibleIngredientMaxs.put(i, Integer.MIN_VALUE);
			strangeIngredientMins.put(i, Integer.MAX_VALUE);
			possibleIngredientMins.put(i, Integer.MAX_VALUE);
		}
		
		CsvController controller = new CsvController();
		List<Recipe> recipes = controller.loadRecipesFromCsv(csvInputFile);

		List<GroupReport> groupreports;
		HashMap<String, List<Integer>> countVectors = new HashMap<String, List<Integer>>();
		HashMap<String, List<Double>> amountVectors = new HashMap<String, List<Double>>();
		HashMap<String, Integer> countSums = new HashMap<String, Integer>();
		HashMap<String, Double> amountSums = new HashMap<String, Double>();
		
		HashMap<String, Integer> countMins = new HashMap<String, Integer>();
		HashMap<String, Double> amountMins = new HashMap<String, Double>();
		
		HashMap<String, Integer> countMaxs = new HashMap<String, Integer>();
		HashMap<String, Double> amountMaxs = new HashMap<String, Double>();
		HashMap<String, Double> countMeans = new HashMap<String, Double>();
		HashMap<String, Double> amountMeans = new HashMap<String, Double>();
		HashMap<String, Double> countMedians = new HashMap<String, Double>();
		HashMap<String, Double> amountMedians = new HashMap<String, Double>();
		HashMap<String, Double> countVariances = new HashMap<String, Double>();
		HashMap<String, Double> amountVariances = new HashMap<String, Double>();
		
		for (Recipe recipe : recipes) {
			groupreports = recipe.getGroupReports(GroupFormatType.SUB);
			groupreports.addAll(recipe.getGroupReports(GroupFormatType.SUPER));
			
			for (GroupReport report : groupreports) {
				String key = report.getGroup().getName();
				double totalAmountInOunces = report.getTotalAmountInOunces();
				int numberOfUniqueIngredients = report.getNumberOfUniqueIngredients();
				if (countVectors.containsKey(key)) {
					countVectors.get(key).add(numberOfUniqueIngredients);
					amountVectors.get(key).add(totalAmountInOunces);
					amountSums.put(key, amountSums.get(key) + totalAmountInOunces);
					countSums.put(key, countSums.get(key) + numberOfUniqueIngredients);
					
					amountMaxs.put(key, Math.max(amountMaxs.get(key), totalAmountInOunces));
					amountMins.put(key, Math.min(amountMins.get(key), totalAmountInOunces));
					countMaxs.put(key, Math.max(countMaxs.get(key), numberOfUniqueIngredients));
					countMins.put(key, Math.min(countMins.get(key), numberOfUniqueIngredients));
				} else {
					List<Integer> newCountVector = new ArrayList<Integer>();
					List<Double> newAmountVector = new ArrayList<Double>();
					newCountVector.add(numberOfUniqueIngredients);
					newAmountVector.add(totalAmountInOunces);
					
					countSums.put(key, numberOfUniqueIngredients);
					amountSums.put(key, totalAmountInOunces);
					amountMaxs.put(key, totalAmountInOunces);
					amountMins.put(key, totalAmountInOunces);
					countMaxs.put(key, numberOfUniqueIngredients);
					countMins.put(key, numberOfUniqueIngredients);
					
					countVectors.put(key, newCountVector);
					amountVectors.put(key, newAmountVector);
				}
			}
			
			for (int i = lowestNGram; i <= highestNGram; i++) {
				int rare = count.countRareNGramsForRecipe(recipe, i, rareThreshold);
				int possible = count.getRecipeNGrams(recipe, i).size();
				
				strangeIngredientCounts.get(i).add(rare);
				possibleIngredientCounts.get(i).add(possible);
				
				strangeIngredientSums.put(i, strangeIngredientSums.get(i) + rare);
				possibleIngredientSums.put(i, possibleIngredientSums.get(i) + possible);
				strangeIngredientMaxs.put(i, Math.max(strangeIngredientMaxs.get(i), rare));
				possibleIngredientMaxs.put(i, Math.max(possibleIngredientMaxs.get(i), possible));
				strangeIngredientMins.put(i, Math.min(strangeIngredientMins.get(i), rare));
				possibleIngredientMins.put(i, Math.min(possibleIngredientMins.get(i), possible));
			}
		}
		for (String key : countVectors.keySet()) {
			List<Integer> countVector = new ArrayList<Integer>(countVectors.get(key));
			List<Double> amountVector = new ArrayList<Double>(amountVectors.get(key));
			double meanCount = countSums.get(key).doubleValue() / ((double) countVector.size());
			countMeans.put(key, meanCount);
			double meanAmount = amountSums.get(key).doubleValue() / ((double) amountVectors.get(key).size());
			amountMeans.put(key, meanAmount);
			
			double sumsquareCount = 0;
			double sumsquareAmount = 0;
			for (int i = 0; i < countVector.size(); i++) {
				sumsquareCount += Math.pow(countVector.get(i) - meanCount, 2);
				sumsquareAmount += Math.pow(amountVector.get(i) - meanAmount, 2);
			}
			
			countVariances.put(key, sumsquareCount / countVector.size());
			amountVariances.put(key, sumsquareAmount / amountVector.size());
			
			countMedians.put(key, getMedianForInteger(countVector));
			amountMedians.put(key, getMedianForDouble(amountVector));
		}
		for (int i = lowestNGram; i <= highestNGram; i++) {
			List<Integer> strangeIngredientVector = new ArrayList<Integer>(strangeIngredientCounts.get(i));
			List<Integer> possibleIngredientVector = new ArrayList<Integer>(possibleIngredientCounts.get(i));
			double meanStrange = strangeIngredientSums.get(i).doubleValue() / ((double) strangeIngredientVector.size());
			strangeIngredientMeans.put(i, meanStrange);
			double meanPossible = possibleIngredientSums.get(i).doubleValue() / ((double) possibleIngredientCounts.get(i).size());
			possibleIngredientMeans.put(i, meanPossible);
			
			double sumsquareStrange = 0;
			double sumsquarePossible = 0;
			for (int j = 0; j < strangeIngredientVector.size(); j++) {
				sumsquareStrange += Math.pow(strangeIngredientVector.get(j) - meanStrange, 2);
				sumsquarePossible += Math.pow(possibleIngredientVector.get(j) - meanPossible, 2);
			}
			
			strangeIngredientVariances.put(i, sumsquareStrange / strangeIngredientVector.size());
			possibleIngredientVariances.put(i, sumsquarePossible / possibleIngredientVector.size());
			
			strangeIngredientMedians.put(i, getMedianForInteger(strangeIngredientVector));
			possibleIngredientMedians.put(i, getMedianForInteger(possibleIngredientVector));
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("Statistic,Mean,Max,Min,Median,StdDev");
		IngredientGroup masterGroup = man.getIngredientGroups();
		for (IngredientGroup superGroup : masterGroup.getChildrenGroups()) {
			String name = superGroup.getName();
			buffer.append("\n" + name + "_Count");
			buffer.append("," + countMeans.get(name) + "," + countMaxs.get(name) + "," + countMins.get(name));
			buffer.append("," + countMedians.get(name) + "," + (Math.sqrt(countVariances.get(name))));
			
			buffer.append("\n" + name + "_Amount");
			buffer.append("," + amountMeans.get(name) + "," + amountMaxs.get(name) + "," + amountMins.get(name));
			buffer.append("," + amountMedians.get(name) + "," + (Math.sqrt(amountVariances.get(name))));
			
			for (IngredientGroup subGroup : superGroup.getChildrenGroups()) {
				name = subGroup.getName();
				buffer.append("\n" + name + "_Count");
				buffer.append("," + countMeans.get(name) + "," + countMaxs.get(name) + "," + countMins.get(name));
				buffer.append("," + countMedians.get(name) + "," + (Math.sqrt(countVariances.get(name))));
				
				buffer.append("\n" + name + "_Amount");
				buffer.append("," + amountMeans.get(name) + "," + amountMaxs.get(name) + "," + amountMins.get(name));
				buffer.append("," + amountMedians.get(name) + "," + (Math.sqrt(amountVariances.get(name))));
			}
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("results/"+ date +"_resultstable_"+ type +"_groups"));
		writer.append(buffer);
		writer.close();
		buffer = new StringBuffer();
		buffer.append("Statistic,Mean,MAX_Mean,Mean_Ratio,Max,MAX_Max,Max_Ratio,Min,MAX_Min,Min_Ratio,Median,MAX_Median,Median_Ratio,StdDev,MAX_StdDev,StdDev_Ratio");
		for (int i = lowestNGram; i <= highestNGram; i++) {
			buffer.append("\nRare_" + i + "-grams");
			double meanRatio = strangeIngredientMeans.get(i).doubleValue() / possibleIngredientMeans.get(i).doubleValue();
			double maxRatio = strangeIngredientMaxs.get(i).doubleValue() / possibleIngredientMaxs.get(i).doubleValue();
			double minRatio = strangeIngredientMins.get(i).doubleValue() / possibleIngredientMins.get(i).doubleValue();
			double medianRatio = strangeIngredientMedians.get(i).doubleValue() / possibleIngredientMedians.get(i).doubleValue();
			double varianceRatio = Math.sqrt(strangeIngredientVariances.get(i)) / Math.sqrt(possibleIngredientVariances.get(i));
			buffer.append("," + (strangeIngredientMeans.get(i) + "," + possibleIngredientMeans.get(i)) + "," + meanRatio + "," + (strangeIngredientMaxs.get(i) + "," + possibleIngredientMaxs.get(i)) + "," + maxRatio + "," + (strangeIngredientMins.get(i) + "," + possibleIngredientMins.get(i)));
			buffer.append("," + minRatio + "," + (strangeIngredientMedians.get(i) + "," + possibleIngredientMedians.get(i)) + "," + medianRatio + "," + (Math.sqrt(strangeIngredientVariances.get(i)) + "," + Math.sqrt(possibleIngredientVariances.get(i))) + "," + varianceRatio);
		}
		
		writer = new BufferedWriter(new FileWriter("results/"+ date +"_resultstable_" + type + "_ngrams"));
		writer.append(buffer);
		writer.close();

	}
	
	private static double getMedianForInteger(List<Integer> vector) {
		double retval = 0;
		
		Collections.sort(vector);
		
		int size = vector.size();
		if (size % 2 == 0) {
			int item1 = size / 2;
			int item2 = item1 + 1;
			
			retval = vector.get(item1) + ((vector.get(item2) - vector.get(item1)) / 2.0);
		} else {
			retval = vector.get((int) Math.ceil(size / 2.0));
		}
		
		return retval;
	}
	private static double getMedianForDouble(List<Double> vector) {
		double retval = 0;
		
		Collections.sort(vector);
		
		int size = vector.size();
		if (size % 2 == 0) {
			int item1 = size / 2;
			int item2 = item1 + 1;
			
			retval = vector.get(item1) + ((vector.get(item2) - vector.get(item1)) / 2.0);
		} else {
			retval = vector.get((int) Math.ceil(size / 2.0));
		}
		
		return retval;
	}
}
