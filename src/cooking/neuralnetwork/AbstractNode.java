package cooking.neuralnetwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

// TODO add momentum term
// Add a copy method so that we can detect convergence
// Need some way of calculated SSE or some other metric
// Need a way of saving the network if the SSE is high enough

public abstract class AbstractNode implements Serializable {
	private static final long serialVersionUID = 1L;
	protected ArrayList<AbstractNode> children;
	protected ArrayList<AbstractNode> parents;
	protected ArrayList<Double> parentWeights;
	protected ArrayList<Double> savedParentWeights;
	protected Double output, net, error;
	protected double learningRate, momentumTerm;
	protected boolean useMomentumTerm;
	protected boolean callChildren;
	
	public abstract void weightUpdate(AbstractNode parent) throws Exception;
	public abstract double getError() throws Exception;
	public abstract double activationFunction(double net);
	public abstract double activationDerivative() throws Exception;
	
	public AbstractNode() {
		children = new ArrayList<AbstractNode>();
		parents = new ArrayList<AbstractNode>();
		parentWeights = new ArrayList<Double>();
		savedParentWeights = new ArrayList<Double>();
		learningRate = 0.1;
		useMomentumTerm = false;
		callChildren = true;
	}
	
	public AbstractNode(double learningRate) {
		children = new ArrayList<AbstractNode>();
		parents = new ArrayList<AbstractNode>();
		parentWeights = new ArrayList<Double>();
		savedParentWeights = new ArrayList<Double>();
		this.learningRate = learningRate;
		useMomentumTerm = false;
		callChildren = true;
	}
	
	public AbstractNode(double learningRate, double momentumTerm) {
		children = new ArrayList<AbstractNode>();
		parents = new ArrayList<AbstractNode>();
		parentWeights = new ArrayList<Double>();
		savedParentWeights = new ArrayList<Double>();
		this.learningRate = learningRate;
		this.momentumTerm = momentumTerm;
		useMomentumTerm = true;
		callChildren = true;
	}
	
	public double getOutput() throws Exception {
		if (output == null) {
			output = activationFunction(getNet());
		}
		return output;
	}
	
	// Prepares for the next training iteration
	public void resetNetwork() {
		net = null;
		output = null;
		error = null;
		callChildren = true;
		
		for (AbstractNode child : getChildren()) {
			child.resetNetwork();
		}
		
		ArrayList<AbstractNode> parents = getParents();
		parentWeights = new ArrayList<Double>();
		Random rand = new Random();
		for (int i = 0; i < parents.size(); i++) {
			parentWeights.add(rand.nextGaussian());
		}
	}
	
	// Prepares for the next input
	public void reset() {
		net = null;
		output = null;
		error = null;
		callChildren = true;
		
		if (getChildren() != null) {
			for (AbstractNode child : getChildren()) {
				child.reset();
			}
		}
	}
	
	public double getNet() throws Exception {
		if (net == null) {
			double sum = 0.0;
			for (AbstractNode child : getChildren()) {
				sum += child.getWeightedOutput(this);
			}
			net = sum;
		}
		return net;
	}
	
	public double getWeightedOutput(AbstractNode parent) throws Exception {
		return getOutput() * parentWeights.get(parents.indexOf(parent));
	}
	
	public void addChild(AbstractNode child) {
		children.add(child);
		child.addParent(this);
	}
	
	public void addParent(AbstractNode parent) {
		Random rand = new Random();
		parents.add(parent);
		parentWeights.add(rand.nextGaussian());
	}
	
	public ArrayList<AbstractNode> getChildren() {
		return children;
	}

	public ArrayList<AbstractNode> getParents() {
		return parents;
	}
	
	public void setOutput(double output) {
		this.output = output;
	}
	
	public void saveNetwork() {
		savedParentWeights = new ArrayList<Double>();
		for (int i = 0; i < parents.size(); i++) {
			savedParentWeights.add(parentWeights.get(i).doubleValue());
		}
		
		for (AbstractNode child : getChildren()) {
			child.saveNetwork();
		}
	}
	
	public void retrieveNetwork() {
		parentWeights = savedParentWeights;
		
		for (AbstractNode child : getChildren()) {
			child.retrieveNetwork();
		}
	}
	
	public String printWeights() {
		String result = "";
		result = parentWeights.toString();
		for (AbstractNode child : getChildren()) {
			result += "\n" + child.printWeights(); 
		}
		return result;
	}
}
