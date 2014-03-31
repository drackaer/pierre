package cooking.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cooking.database.CsvController;
import cooking.database.InspiringSetType;
import cooking.database.DbManager;
import cooking.database.GroupReport;
import cooking.evaluation.EvaluatorType;
import cooking.evaluation.IEvaluator;
import cooking.evaluation.InterpolatedEvaluator;
import cooking.evaluation.InterpolatedNeuralNetworkEvaluator;
import cooking.evaluation.SuperGroupNeuralNetworkEvaluator;
import cooking.generation.SgaGenerator;
import cooking.generation.evolutionary.FitnessProportionSelector;
import cooking.generation.evolutionary.SimpleGeneticAlgorithm;
import cooking.generation.evolutionary.TwoPointCrossOver;
import cooking.generation.evolutionary.mutation.StatsBasedBasicMutator;
import cooking.recipe.GroupFormatType;
import cooking.recipe.Recipe;

public class RecipeGenerator {

	private static boolean xmlwrite = false;
	private static boolean printNames = false;
	private static int numIterations = 150;
	private static int seedPopulationSize = 150;
	private static final String xmlFileName = "PIERRE/recipes_online.xml";
	private static int numRestarts = 50;
	private static int numRecipesToPrint = 10;
	
	public static void main(String[] args) throws Exception {
		DbManager manager = DbManager.getSingleton();
		manager.loadData(InspiringSetType.FULL, true);
		//System.out.println("Pruned # Ingredients: " + manager.pruneIngredients());
		
		List<Recipe> recipes = new ArrayList<Recipe>();
		String csvFile = "results/2012-10-25_fullinspiring_chilieval";
		IEvaluator eval = new InterpolatedNeuralNetworkEvaluator(EvaluatorType.CHILI);
		
		for (int k = 0; k < numRestarts; k++) {
			System.out.println("Beginning Iteration #" + (k+1));
			SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm();
			
			sga.setMutationRate(.4);
			
			sga.setEvaluator(eval);
			
			sga.setSelector(new FitnessProportionSelector());
			
			sga.setNumberOfIterations(numIterations);
			TwoPointCrossOver cross = new TwoPointCrossOver();
			sga.setCrossOverOperator(cross);
			
			StatsBasedBasicMutator mutator = new StatsBasedBasicMutator();
			sga.setMutationOperator(mutator);
			
			SgaGenerator generator = new SgaGenerator();
			
			List<Recipe> seedPopulation = manager.getRandomSampleOfRecipes(seedPopulationSize);
			
			generator.setSeedPopulation(seedPopulation);
			generator.setSimpleGeneticAlgorithm(sga);
			
			generator.generateRecipe();
			
			//System.out.println("Recipes for Full Evaluator and Full Stats");
			//System.out.println("Top 10 Recipes:");
			BufferedWriter bw = null;
			if(xmlwrite){
				try{
					Scanner scan = new Scanner(new File(xmlFileName));
					String fileContents = "";
					while(scan.hasNextLine()) fileContents += scan.nextLine() + "\n";
					bw = new BufferedWriter(new FileWriter(xmlFileName,false));
					
					scan = new Scanner(fileContents);
					String line;
					if(scan.hasNextLine()){
						line = scan.nextLine();
						while(!line.equals("</data>")){
							bw.write(line + "\n");
							line = scan.nextLine();						
						}
					}
					else{
						//file is empty
						bw.write("<data>\n");
					}
					
				}
				catch(Exception e){
					System.out.println(e);
					bw = new BufferedWriter(new FileWriter(xmlFileName,true));
					bw.write("<data>\n");
				}
			}
			
			for (int i = 0; i < numRecipesToPrint; i++)
			{
				Recipe cur = generator.getSimpleGeneticAlgorithm().getPopulation().get(i);
				cur.setTemporaryEvaluation(eval.evaluateRecipe(cur));
				System.out.println((i + 1) + ". " + cur.toLongString());
				
				if(xmlwrite){
					bw.write(cur.toOnlineString() + "\n");
				}
				recipes.add(cur);
			}
	
			if (xmlwrite) {
				bw.write("</data>");
				bw.close();
			}
	
			if (printNames) {
				System.out.println("\nRecipes names:");
				for (Recipe cur: generator.getSimpleGeneticAlgorithm().getPopulation())
				{
					System.out.println(cur.getName());
				}
			}
		}
		CsvController controller = new CsvController();
		//controller.saveToCsv(recipes, csvFile);
	}

}
