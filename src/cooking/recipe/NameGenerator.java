package cooking.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cooking.database.IngredientGroup;

public class NameGenerator {

	public final String DEFAULT_NAME = "Pierre's Special Surprise";
	private Logger logger = Logger.getLogger(this.getClass());
	
	public double adjectiveRate = .8;
	public double endingRate = .3;
	public double randomIngredientRate = .15;
	
	public String generateName(Recipe recipe) {
		String name = "";
		
		try {
			if (random.nextDouble() < adjectiveRate) {
				name = getAppropriateAdjective(recipe) + " ";
			}
			
			List<MeasuredIngredient> list = recipe.getIngredientsOrderedByAmount();
			
			Ingredient first = list.get(0).getIngredient();
			
			String ingredients = first.getName();
			
			if (list.size() >= 2) {

				Ingredient second;
				
				if (random.nextDouble() < randomIngredientRate) {
					// don't randomly choose the first
					int randomIndex = random.nextInt(list.size() - 1) + 1;
					second = list.get(randomIndex).getIngredient();
					
				}
				else {
					second = list.get(1).getIngredient();
				}
	
				ingredients = combineIngredientNames(first, second);
			}
			
			name += ingredients;
			
			if (random.nextDouble() < endingRate) {
				List<String> allEndings = getAllEndings();
				name += " " + allEndings.get(random.nextInt(allEndings.size()));
			}
			
			name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
		}
		catch (Exception ex) {
			logger.error("Exception in generated name, using default name instead.  Details: " + ex.getMessage());
			ex.printStackTrace();
			name = DEFAULT_NAME;
		}
		
		return name;
	}
	
	private String getAppropriateAdjective(Recipe r) {
		List<String> list = getAllGenericAdjectives();
		
		GroupFormatRecipe groupFormat = r.getGroupFormat(GroupFormatType.SUB);
		for (IngredientGroup group : groupFormat.getIngredientGroups()) {
			if (group.getName().toLowerCase().equals("chilis")) {
				list.addAll(getAllSpicyAdjectives());
				// by not breaking, if we have multiple items in this category, it will increase
				// the chances of picking one of these adjectives
			}
		}
		
		for (IngredientGroup group : groupFormat.getIngredientGroups()) {
			if (group.getName().toLowerCase().equals("sweetener")) {
				list.addAll(getAllSweetAdjectives());
				// by not breaking, if we have multiple items in this category, it will increase
				// the chances of picking one of these adjectives
			}
		}
		
		return list.get(random.nextInt(list.size()));
	}
	
	private String convertToAdjective(String input) {
		String word = input.replaceFirst("(.*[^eiy])[eiy]*$", "$1");
		
		word += "y";
		
		return word;
	}
	
	private String combineIngredientNames(Ingredient first, Ingredient second) {
		String firstName = getNameToUse(first);
		String secondName = getNameToUse(second);
		
		List<String> list = new ArrayList<String>();
		
		if (firstName.equals(secondName)) {
			list.add(firstName);
		}
		else {
			list.add(convertToAdjective(secondName) + " " + firstName);
			list.add(firstName + " with " + secondName);
			list.add(secondName + " over " + firstName);
		}
		
		return list.get(random.nextInt(list.size()));
	}
	
	private String getNameToUse(Ingredient i) {
		List<String> names = i.getAlternateNames();
		names.add(i.getName());
		
		String name = names.get(random.nextInt(names.size()));

		String lastWord = name;
		if (lastWord.contains(" ")) {
			lastWord = lastWord.substring(lastWord.lastIndexOf(" ") + 1);
		}
		
		return lastWord;
		
	}
	
	public Random random = new Random();

	public List<String> getAllAdjectives() {
		List<String> wordList = new ArrayList<String>();
		
		wordList.addAll(getAllGenericAdjectives());
		wordList.addAll(getAllSpicyAdjectives());
		wordList.addAll(getAllSweetAdjectives());
		
		return wordList;
	}
	
	public List<String> getAllGenericAdjectives() {
		List<String> wordList = new ArrayList<String>();
		
		wordList.add("exotic");
		wordList.add("delicious");
		wordList.add("homestyle");
		wordList.add("delightful");
		wordList.add("divine");
		wordList.add("exquisite");
		wordList.add("heavenly");
		wordList.add("luscious");
		wordList.add("mouth-watering");
		wordList.add("scrumptious");
		wordList.add("yummy");
		wordList.add("zesty");
		wordList.add("to die for");
		wordList.add("saucy");
		wordList.add("pierre's best");
		
		return wordList;
	}
	
	public List<String> getAllSpicyAdjectives() {
		List<String> wordList = new ArrayList<String>();
		
		wordList.add("spicy");
		wordList.add("smoking hot");
		wordList.add("fiery");
		wordList.add("hot");
		wordList.add("peppery");
		wordList.add("zippy");
		wordList.add("not your grandma's");
		wordList.add("red hot");
		wordList.add("cajun");
		
		return wordList;
	}
	
	public List<String> getAllSweetAdjectives() {
		List<String> wordList = new ArrayList<String>();
		
		wordList.add("sweet");
		wordList.add("honeyed");
		wordList.add("nectarous");
		wordList.add("sweetened");
		wordList.add("toothsome");
		
		return wordList;
	}
	
	public List<String> getAllEndings() {
		List<String> wordList = new ArrayList<String>();
		
		wordList.add("fit for a king");
		wordList.add("of pure joy");
		wordList.add("stew");
		wordList.add("cacciatore");
		wordList.add("blend");
		wordList.add("spectacular");
		wordList.add("a la carte");
		wordList.add("surpise");
		wordList.add("on a shingle");
		wordList.add("ready to eat");
		
		return wordList;
	}
}
