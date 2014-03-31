package cooking.evaluation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cooking.database.DbManager;

public class NeuralNetworkEvaluatorGenerator {
	
	private static NeuralNetworkEvaluatorGenerator instance;
	
	private String subGroupSerializedLocation;
	private String superGroupSerializedLocation;
	
	private NeuralNetworkEvaluatorGenerator() {
	}
	
	public static NeuralNetworkEvaluatorGenerator getSingleton() {
		if (instance == null) {
			instance = new NeuralNetworkEvaluatorGenerator();
		}
		
		return instance;
	}
	
	public SubGroupNeuralNetworkEvaluator getSubGroupEvaluator(EvaluatorType type) throws Exception {
		SubGroupNeuralNetworkEvaluator evaluator = null;
		
		switch (type) {
		case CHILI:
			subGroupSerializedLocation = "data/subgroupchilinet.ser";
			break;
		case FULL:
			subGroupSerializedLocation = "data/subgroupneuralnet.ser";
			break;
		case DEGENERATE:
			subGroupSerializedLocation = "data/degenerateneuralnet.ser";
			break;
		default:
			throw new Exception("Evaluator Type Not Recognized");
		}
		
		try {
			FileInputStream fileStream = new FileInputStream(subGroupSerializedLocation);
			ObjectInputStream inputStream = new ObjectInputStream(fileStream);
			evaluator = (SubGroupNeuralNetworkEvaluator) inputStream.readObject();
			System.out.println("Sub Group Evaluator Successfully Loaded From File.");
			inputStream.close();
		} catch (Exception e) {
			System.out.println("Sub Group Evaluator Was Not Successfully Loaded From File.  Attempting to train a new evaluator.");
			try {
				DbManager.getSingleton().loadRandomRecipes();
				evaluator = new SubGroupNeuralNetworkEvaluator();
				FileOutputStream fileStream = new FileOutputStream(subGroupSerializedLocation);
				ObjectOutputStream outputStream = new ObjectOutputStream(fileStream);
				outputStream.writeObject(evaluator);
				outputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return evaluator;
	}
	
	public SuperGroupNeuralNetworkEvaluator getSuperGroupEvaluator(EvaluatorType type) throws Exception {
		SuperGroupNeuralNetworkEvaluator evaluator = null;
		
		switch (type) {
		case CHILI:
			superGroupSerializedLocation = "data/supergroupchilinet.ser";
			break;
		case FULL:
			superGroupSerializedLocation = "data/supergroupneuralnet.ser";
			break;
		case DEGENERATE:
			superGroupSerializedLocation = "data/degenerateneuralnet.ser";
			break;
		default:
			throw new Exception("Evaluator Type Not Recognized");
		}
		
		try {
			FileInputStream fileStream = new FileInputStream(superGroupSerializedLocation);
			ObjectInputStream inputStream = new ObjectInputStream(fileStream);
			evaluator = (SuperGroupNeuralNetworkEvaluator) inputStream.readObject();
			System.out.println("Super Group Evaluator Successfully Loaded From File.");
			inputStream.close();
		} catch (Exception e) {
			System.out.println("Super Group Evaluator Was Not Successfully Loaded From File.  Attempting to train a new evaluator.");
			try {
				DbManager.getSingleton().loadRandomRecipes();
				evaluator = new SuperGroupNeuralNetworkEvaluator();
				FileOutputStream fileStream = new FileOutputStream(superGroupSerializedLocation);
				ObjectOutputStream outputStream = new ObjectOutputStream(fileStream);
				outputStream.writeObject(evaluator);
				outputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return evaluator;
	}
}
