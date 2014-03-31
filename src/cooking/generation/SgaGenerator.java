package cooking.generation;

import java.util.List;

import org.apache.log4j.Logger;

import cooking.generation.evolutionary.SimpleGeneticAlgorithm;
import cooking.recipe.Recipe;

/**
 * @author sburton
 *
 *	Uses a SimpleGeneticAlgorithm to generate the next recipe
 */
public class SgaGenerator implements IGenerator {

	public SgaGenerator() {
		sga = new SimpleGeneticAlgorithm();
		
	}
	
	private static Logger logger = Logger.getLogger(SgaGenerator.class);
	
	private SimpleGeneticAlgorithm sga;
	private List<Recipe> seedPopulation;
	
	@Override
	public Recipe generateRecipe() {
		Recipe bestRecipe = null;
		
		try {
			sga.setPopulation(getSeedPopulation());
			sga.setPopulationSize(getSeedPopulation().size());
			
			sga.run();
			
			bestRecipe = sga.getPopulation().get(0);
			
		} catch (Exception e) {
			logger.error("Error running genetic algorithm. " + e.getMessage() + " Stack trace:" + e.getStackTrace());
			e.printStackTrace();
		}
		
		return bestRecipe;
	}

	public void setSimpleGeneticAlgorithm(SimpleGeneticAlgorithm sga) {
		this.sga = sga;
	}

	public SimpleGeneticAlgorithm getSimpleGeneticAlgorithm() {
		return sga;
	}

	public void setSeedPopulation(List<Recipe> seedPopulation) {
		this.seedPopulation = seedPopulation;
	}

	public List<Recipe> getSeedPopulation() {
		return seedPopulation;
	}

}
