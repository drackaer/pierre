package cooking.neuralnetwork;

public class HiddenBiasNode extends HiddenNode {
	private static final long serialVersionUID = 1L;

	public HiddenBiasNode() {
		super();
	}
	
	public HiddenBiasNode(double learningRate) {
		super(learningRate);
	}
	
	public HiddenBiasNode(double learningRate, double momentumTerm) {
		super(learningRate, momentumTerm);
	}
	
	public double getOutput() {
		return 1;
	}
}
