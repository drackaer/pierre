package cooking.neuralnetwork;

public class InputNode extends AbstractSigmoidNode {

	private static final long serialVersionUID = 1L;

	public InputNode() {
		super();
	}
	
	public InputNode(double learningRate) {
		super(learningRate);
	}
	
	public InputNode(double learningRate, double momentumTerm) {
		super(learningRate, momentumTerm);
	}
	
	public void weightUpdate(AbstractNode parent) throws Exception {
		double weightUpdate = learningRate * parent.getError() * getOutput();
		int parentIndex = parents.indexOf(parent);
		parentWeights.set(parentIndex, weightUpdate + parentWeights.get(parentIndex));
	}

	public double getError() {
		System.out.println("***FAIL get error");
		return 0;
	}
	
	public double getNet() throws Exception {
		throw new Exception("GetNet() called on an input node.");
	}
	
	private int inputIndex;
	
	public int getInputIndex() {
		return inputIndex;
	}
	
	public void setInputIndex(int inputIndex) {
		this.inputIndex = inputIndex;
	}
}
