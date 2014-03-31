package cooking.recipe.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cooking.database.CsvController;
import cooking.database.InspiringSetType;
import cooking.database.DbManager;
import cooking.recipe.MeasuredIngredient;
import cooking.recipe.Recipe;

// Counts the number of times ingredients co-occur in a given set of recipes
public class RecipeNGramCounter {
	private static RecipeNGramCounter instance;
	private HashMap<Integer, HashMap<String, Integer>> countMapByNGramSize;
	
	private RecipeNGramCounter() {
		countMapByNGramSize = new HashMap<Integer, HashMap<String,Integer>>();
	}
	
	public static RecipeNGramCounter getInstance() {
		if (instance == null) {
			instance = new RecipeNGramCounter();
		}
		return instance;
	}
	
	public void countNGrams(List<Recipe> recipes, int n) {
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		
		for (Recipe recipe : recipes) {
			Set<String> ngrams = getRecipeNGrams(recipe, n);
			
			for (String ngram : ngrams) {
				if (countMap.containsKey(ngram)) {
					countMap.put(ngram, countMap.get(ngram) + 1);
				} else {
					countMap.put(ngram, 1);
				}
			}
		}
		
		countMapByNGramSize.put(n, countMap);
	}
	
	public HashMap<String, Integer> getCountMapForNGramSize(int n) {
		return countMapByNGramSize.get(new Integer(n));
	}

	public Set<String> getRecipeNGrams(Recipe recipe, int n) {
		
		Set<String> ngrams = new HashSet<String>();
		if (recipe.getIngredients().size() < n) return ngrams;

		List<String> ingredients = new ArrayList<String>();
		for (MeasuredIngredient ingredient : recipe.getIngredients()) {
			String name = ingredient.getIngredient().getName();
			ingredients.add(name);
		}
		
		// Works like a rolling counter
		int[] counters = new int[n];
		for (int i = 0; i < n; i++) {
			counters[i] = i;
		}
		
		while(counters[0] < ingredients.size() - (n-1)) {
			Set<String> ingset = new HashSet<String>();
			for (int i = 0; i < n; i++) {
				ingset.add(ingredients.get(counters[i]));
			}
			if (ingset.size() == n) {
				List<String> newNGram = new ArrayList<String>(ingset);
				Collections.sort(newNGram);
				String ngram = newNGram.get(0);
				for (int k = 1; k < newNGram.size(); k++) {
					ngram += ":" + newNGram.get(k);
				}
				ngrams.add(ngram);
			} else {
				System.out.println("**********It Happened**********");
			}
			
			// update rolling counter
			Boolean stop = false;
			Boolean correctCounter = false;
			int current = n-1;
			while(!stop && current != -1) {
				counters[current]++;
				int shift = (n-1) - current;
				if (counters[current] == ingredients.size() - shift) {
					current--;
					correctCounter = true;
				} else {
					stop = true;
				}
			}
			
			if (correctCounter && current != -1) {
				for (int i = current + 1; i < n; i++) {
					counters[i] = counters[i - 1] + 1;
				}
			}
		}
		
		/*for (int i = 0; i < ingredients.size(); i++) {
			String ingredient1 = ingredients.get(i);
			for (int j = 0; j+(n-1) < ingredients.size(); j++) {
				Set<String> ingset = new HashSet<String>();
				ingset.add(ingredient1);
				for (int k = 0; k < n-1; k++) {
					ingset.add(ingredients.get(j+k));
				}
				if (ingset.size() == n) {
					List<String> newNGram = new ArrayList<String>(ingset);
					Collections.sort(newNGram);
					String ngram = newNGram.get(0);
					for (int k = 1; k < newNGram.size(); k++) {
						ngram += ":" + newNGram.get(k);
					}
					ngrams.add(ngram);
				}
			}
		}*/
		
		return ngrams;
	}
	
	public Set<String> getRareNGramsLessThanN(Recipe recipe, int n, int rareThreshold) {
		Set<String> rareLessThanNGrams = new HashSet<String>();
		
		for (int i = 1; i < n; i++) {
			HashMap<String, Integer> map = getCountMapForNGramSize(i);
			Set<String> ngrams = getRecipeNGrams(recipe, i);
			for (String ngram : ngrams) {
				if (map.containsKey(ngram)) {
					int count = map.get(ngram);
					if (count < rareThreshold) {
						rareLessThanNGrams.add(ngram);
					}
				} else {
					rareLessThanNGrams.add(ngram);
				}
			}
		}
		
		return rareLessThanNGrams;
	}
	
	private boolean ngramSupersetOfNgrams(String ngram, Set<String> ngrams) {
		boolean retval = false;
		
		for (String currentNgram : ngrams) {
			String[] splitNgram = currentNgram.split(":");
			
			boolean containsAll = true;
			for (String ingredient : splitNgram) {
				if (!ngram.contains(ingredient)) {
					containsAll = false;
					break;
				}
			}
			if (containsAll) {
				retval = true;
				break;
			}
		}
		
		return retval;
	}
	
	public int countRareNGramsForRecipe(Recipe recipe, int n, int rareThreshold) throws Exception {
		int retval = 0;
		HashMap<String, Integer> map = getCountMapForNGramSize(n);
		if (map == null) throw new Exception("Count Map Not Initialized.");
		
		Set<String> ngrams = getRecipeNGrams(recipe, n);
		Set<String> rareLessThanNGrams = getRareNGramsLessThanN(recipe, n, rareThreshold);
		
		for (String ngram : ngrams) {
			if (map.containsKey(ngram)) {
				int count = map.get(ngram);
				if (count < rareThreshold) {
					if (!ngramSupersetOfNgrams(ngram, rareLessThanNGrams)) { retval++; }
					if (n == 1) {
						System.out.println(ngram);
					}
				}
			} else {
				if (!ngramSupersetOfNgrams(ngram, rareLessThanNGrams)) { retval++; }
				if (n == 1) {
					System.out.println(ngram);
				}
			}
		}
		
		return retval;
	}
	
	public static void main (String[] args) throws Exception {
		RecipeNGramCounter inst = getInstance();
		DbManager manager = DbManager.getSingleton();
		manager.loadData(InspiringSetType.FULL, true);
		CsvController controller = new CsvController();
		String csvInputFile = "results/2011-7-5_chilieval_fullstats";
		List<Recipe> recipes = controller.loadRecipesFromCsv(csvInputFile);
		
		//inst.countNGrams(recipes, 1);
		//inst.countNGrams(recipes, 2);
		//inst.countNGrams(recipes, 3);
		inst.countNGrams(recipes, 4);
		//inst.countNGrams(recipes, 5);

		
		//HashMap<String, Integer> map1 = inst.getCountMapForNGramSize(1);
		//HashMap<String, Integer> map2 = inst.getCountMapForNGramSize(2);
		//HashMap<String, Integer> map3 = inst.getCountMapForNGramSize(3);
		HashMap<String, Integer> map4 = inst.getCountMapForNGramSize(4);
		//HashMap<String, Integer> map5 = inst.getCountMapForNGramSize(5);
		
		for (int i = 4; i <= 4; i++){
			System.out.print(inst.countRareNGramsForRecipe(recipes.get(0), i, 1));
			System.out.println(":" + inst.getRecipeNGrams(recipes.get(0), i).size());
		}
	}
}
