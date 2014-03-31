package cooking.neuralnetwork;

import java.io.Serializable;

public class OutputRegressionNode extends OutputNode implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public OutputRegressionNode() {
		super();
	}
	
	public OutputRegressionNode(double learningRate) {
		super(learningRate);
	}
	
	public OutputRegressionNode(double learningRate, double momentumTerm) {
		super(learningRate, momentumTerm);
	}
	
	public double getTarget() {
		return currentTarget;
	}
}
