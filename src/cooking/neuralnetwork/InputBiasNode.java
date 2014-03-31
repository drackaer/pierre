package cooking.neuralnetwork;

public class InputBiasNode extends InputNode {
	private static final long serialVersionUID = 1L;

	public InputBiasNode() {
		super();
	}
	
	public InputBiasNode(double learningRate) {
		super(learningRate);
	}
	
	public InputBiasNode(double learningRate, double momentumTerm) {
		super(learningRate, momentumTerm);
	}
	
	public double getOutput() {
		return 1;
	}
}
