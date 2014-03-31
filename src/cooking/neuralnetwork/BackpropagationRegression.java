package cooking.neuralnetwork;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackpropagationRegression implements Serializable {

	private static final long serialVersionUID = 1L;
	private int stoppingCriteria;
	private OutputRegressionNode outputNode;
	private ArrayList<ArrayList<Double>> trainSet;
	private ArrayList<ArrayList<Double>> testSet;
	private ArrayList<ArrayList<Double>> validSet;
	
	public BackpropagationRegression(ArrayList<ArrayList<Double>> trainingData, ArrayList<ArrayList<Double>> testData,
			ArrayList<ArrayList<Double>> validationData, int numHidden, double learningRate, int stoppingCriteria) {
		trainSet = trainingData;
		testSet = testData;
		validSet = validationData;
		
		outputNode = new OutputRegressionNode(learningRate);
		inputLayer = new ArrayList<InputNode>();
		
		this.stoppingCriteria = stoppingCriteria;
		
		HiddenBiasNode hiddenBiasNode = new HiddenBiasNode(learningRate);
		
		outputNode.addChild(hiddenBiasNode);
		
		for (int i = 0; i < trainSet.get(0).size() - 1; i++) {
			InputNode node = new InputNode(learningRate);
			inputLayer.add(node);
			node.setInputIndex(i);
		}
		InputBiasNode inputBiasNode = new InputBiasNode(learningRate);
				
		for (int i = 0; i < numHidden; i++) {
			HiddenNode node = new HiddenNode(learningRate);
			outputNode.addChild(node);
			
			for (InputNode inNode : inputLayer) {
				node.addChild(inNode);
			}
			node.addChild(inputBiasNode);
		}
	}
	
	// One output layer per different output dimension, one output node in each layer
	// for each output class in that dimension
	private List<InputNode> inputLayer;
	
	public double evaluateInstance(ArrayList<Double> instance) throws Exception {
		outputNode.reset();
		for (InputNode in : inputLayer) {
			in.setOutput(instance.get(in.getInputIndex()));
		}
		return outputNode.getOutput();
	}

	// Restores to the untrained state
	public void reset() {
		outputNode.resetNetwork();
	}
	
	public double getSSE() throws Exception {
		double sse = 0;
		for (ArrayList<Double> instance: validSet) {
			double result = evaluateInstance(instance);
			sse += Math.pow(result - instance.get(instance.size() - 1), 2);
		}
		return sse;
	}
	
	public double evalAccuracy() throws Exception
	{	
		double target;
		double output;
		double sse = 0;
		
		for (int i = 0; i < testSet.size(); i++) {
			output = evaluateInstance(testSet.get(i));
			target = testSet.get(i).get(testSet.get(i).size() - 1);
			sse = Math.pow(target - output, 2);
		}
		
		return sse;
	}
	
	public void train() throws Exception {
		double lowestError = Double.MAX_VALUE;
		int epochsWithoutChange = 0;
		while(epochsWithoutChange < stoppingCriteria) // Stopping criteria?
		{
			for (ArrayList<Double> dataItem : trainSet) {
				evaluateInstance(dataItem);
				
				outputNode.setCurrentTarget(dataItem.get(dataItem.size() - 1));
				outputNode.weightUpdate(null);
			}
			double sse = getSSE();
			if (sse < lowestError) {
				lowestError = sse;
				outputNode.saveNetwork();
				epochsWithoutChange = 0;
			}
			else {
				epochsWithoutChange++;
			}
			
			trainSet = randomizeData(trainSet);
			testSet = randomizeData(testSet);
		}
			outputNode.retrieveNetwork();
	}

	private ArrayList<ArrayList<Double>> randomizeData(ArrayList<ArrayList<Double>> data) {
		ArrayList<ArrayList<Double>> tempData = new ArrayList<ArrayList<Double>>();
		Random rand = new Random();
		while (data.size() > 0) {
			int num = rand.nextInt(data.size());
			tempData.add(data.remove(num));
		}
		data = tempData;
		return data;
	}

	public void OutputWeights() {
		System.out.println(outputNode.printWeights());
	}
}
