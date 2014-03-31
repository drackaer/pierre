package cooking.generation.evolutionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cooking.database.DbManager;
import cooking.evaluation.DefaultEvaluator;
import cooking.evaluation.IEvaluator;
import cooking.generation.evolutionary.mutation.BasicMutator;
import cooking.generation.evolutionary.mutation.IMutationOperator;
import cooking.recipe.Recipe;

public class SimpleGeneticAlgorithm {

	public SimpleGeneticAlgorithm() {
		setEvaluator(new DefaultEvaluator());
		setSelector(new RandomSelector());
		setCrossOverOperator(new TwoPointCrossOver());
		setMutationOperator(new BasicMutator());
	}
	
	private double mutationRate = 0.2;
	private List<Recipe> population;
	private double replacementRate = .5;
	private int populationSize = 1000;
	private int numberOfIterations = 1000;
	private boolean keepAtConstantSize = true;
	private double constantSizeInOunces = DbManager.getScaleSize();
	private double minIngredientSizeThreshold = 0.1;
	
	private IEvaluator evaluator = null;
	private ISelector selector = null;
	private ICrossOverOperator crossOverOperator = null;
	private IMutationOperator mutationOperator = null;
	
	private Logger logger = Logger.getLogger(this.getClass());

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setPopulation(List<Recipe> population) {
		this.population = population;
	}

	public List<Recipe> getPopulation() {
		return population;
	}
	
	public void setReplacementRate(double replacementRate) {
		this.replacementRate = replacementRate;
	}

	public double getReplacementRate() {
		return replacementRate;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setEvaluator(IEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public IEvaluator getEvaluator() {
		return evaluator;
	}

	public void setSelector(ISelector selector) {
		this.selector = selector;
	}

	public ISelector getSelector() {
		return selector;
	}

	public void setNumberOfIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}

	public int getNumberOfIterations() {
		return numberOfIterations;
	}

	/**
	 * Runs the genetic algorithm for getNumberOfIterations() iterations
	 * @throws Exception
	 */
	public void run() throws Exception {
		// TODO: initialize population -- if necessary
		for (Recipe r : population) {
			r.setTemporaryEvaluation(evaluator.evaluateRecipe(r));
		}
		
		for (int i = 0; i < getNumberOfIterations(); i++) {
			logger.debug("Running GA iteration: " + i);
			runIteration();
		}
	}
	
	/**
	 * Runs one iteration of the genetic algorithm
	 * 
	 * @throws Exception
	 */
	private void runIteration() throws Exception {
		// produce new generation
		List<Recipe> newGeneration = produceNewGeneration(population);
		
		// perform replacement
		population = performReplacement(population, newGeneration);
	}

	/**
	 * creates a new list with getReplacementRate() amount of the newGeneration and 1-getReplacementRate()
	 * of the old generation.
	 * 
	 * ASSUMPTION: both lists are sorted by fitness
	 * 
	 * @param oldGeneration
	 * @param newGeneration
	 * @return a sorted list of the new population
	 * @throws Exception 
	 */
	private List<Recipe> performReplacement(List<Recipe> oldGeneration, List<Recipe> newGeneration) throws Exception {
		List<Recipe> newPopulation = new ArrayList<Recipe>();

		if (oldGeneration.size() != getPopulationSize() || newGeneration.size() != getPopulationSize()) {
			throw new Exception("Error population sizes were not correct");
		}
		
		// NOTE: assuming lists are already sorted
		int numberOfNew = (int)Math.round(getReplacementRate() * getPopulationSize());
		int numberOfOld = getPopulationSize() - numberOfNew;
		
		newPopulation.addAll(oldGeneration.subList(0, numberOfOld));
		newPopulation.addAll(newGeneration.subList(0, numberOfNew));
		
		if (newPopulation.size() != getPopulationSize()) {
			throw new Exception("Error in performing replacement, did not produce the right number in the new population");
		}
		
		sortRecipesByFitness(newPopulation);
		
		return newPopulation;
	}
	
	
	/**
	 * Sorts the given list of recipes by fitness in descending order using getEvaluator()
	 * @param list
	 */
	private void sortRecipesByFitness(List<Recipe> list) {
		Collections.sort(list, new RecipeFitnessSorter(getEvaluator()));
	}

	/**
	 * Produces a new generation of recipes.
	 * 
	 * The returned list is sorted by fitness.
	 * 
	 * @param oldGeneration
	 * @return
	 * @throws Exception 
	 */
	private List<Recipe> produceNewGeneration(List<Recipe> oldGeneration) throws Exception {
		List<Recipe> newGeneration = new ArrayList<Recipe>();

		for (int i = 0; i < getPopulationSize(); i++) {
			// select parents (note they could be the same)
			Recipe parent1 = selectParent(oldGeneration);
			Recipe parent2 = selectParent(oldGeneration);
			
			//logger.info("Parent 1: " + parent1.toLongString());
			//logger.info("Parent 2: " + parent2.toLongString());
			
			// perform crossover
			Recipe newRecipe = getCrossOverOperator().crossOver(parent1, parent2);
			
			// check if we have a mutation
			if (random.nextDouble() <= getMutationRate()) {
				newRecipe = getMutationOperator().performMutation(newRecipe);
			}

			if (isKeepAtConstantSize()) {
				newRecipe = newRecipe.getScaledVersion(getConstantSizeInOunces());
			}
			
			boolean madeChange = newRecipe.removeIngedientsBelowThreshold(getMinIngredientSizeThreshold());

			if (madeChange && isKeepAtConstantSize()) {
				newRecipe = newRecipe.getScaledVersion(getConstantSizeInOunces());
			}
			
			newRecipe.combineSameIngredients();
			newRecipe.clearTemporaryEvaluation();

			if (newRecipe.getIngredients().size() == 0) {
				logger.debug("generated a new recipe without ingredients, using parent instead.");
				// if the new recipe has no ingredients, then let's just pass the parent on to the next gen
				newGeneration.add(parent1);
			}
			else {
				newGeneration.add(newRecipe);
			}
		}
		
		sortRecipesByFitness(newGeneration);
		
		return newGeneration;
	}

	private Recipe selectParent(List<Recipe> oldGeneration) {
		return getSelector().selectNextParent(oldGeneration);
	}
	
	public void setKeepAtConstantSize(boolean keepAtConstantSize) {
		this.keepAtConstantSize = keepAtConstantSize;
	}

	public boolean isKeepAtConstantSize() {
		return keepAtConstantSize;
	}

	public void setConstantSizeInOunces(double constantSizeInOunces) {
		this.constantSizeInOunces = constantSizeInOunces;
	}

	public double getConstantSizeInOunces() {
		return constantSizeInOunces;
	}

	public void setCrossOverOperator(ICrossOverOperator crossOverOperator) {
		this.crossOverOperator = crossOverOperator;
	}

	public ICrossOverOperator getCrossOverOperator() {
		return crossOverOperator;
	}

	public void setMutationOperator(IMutationOperator mutationOperator) {
		this.mutationOperator = mutationOperator;
	}

	public IMutationOperator getMutationOperator() {
		return mutationOperator;
	}

	public void setMinIngredientSizeThreshold(double minIngredientSizeThreshold) {
		this.minIngredientSizeThreshold = minIngredientSizeThreshold;
	}

	public double getMinIngredientSizeThreshold() {
		return minIngredientSizeThreshold;
	}

	public Random random = new Random();
}
