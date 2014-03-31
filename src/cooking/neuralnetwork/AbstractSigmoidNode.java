package cooking.neuralnetwork;

public abstract class AbstractSigmoidNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public AbstractSigmoidNode() {
		super();
	}
	
	public AbstractSigmoidNode(double learningRate) {
		super(learningRate);
	}
	
	public AbstractSigmoidNode(double learningRate, double momentumTerm) {
		super(learningRate, momentumTerm);
	}
	
	public double activationFunction(double net) {
		return 1.0 / (1 + Math.exp(net*-1));
	}
	
	public double activationDerivative() throws Exception {
		return getOutput() * (1 - getOutput());
	}
}
