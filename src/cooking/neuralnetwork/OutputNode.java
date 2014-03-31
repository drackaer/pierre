package cooking.neuralnetwork;

import java.io.Serializable;

public class OutputNode extends AbstractSigmoidNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
	protected Double currentTarget; // Class that should have been chosen
	protected Double targetClass; // Class I am responsible for
	
	public OutputNode() {
		super();
	}
	
	public OutputNode(double learningRate) {
		super(learningRate);
	}
	
	public OutputNode(double learningRate, double momentumTerm) {
		super(learningRate, momentumTerm);
	}
	
	public double getWeightedOutput(AbstractNode node) throws Exception {
		return getOutput();
	}
	
	public void addParent(AbstractNode parent) {
		assert(false);
	}

	public void weightUpdate(AbstractNode parent) throws Exception {
		for (AbstractNode child : getChildren()) {
			child.weightUpdate(this);
		}
	}
	
	public double getError() throws Exception {
		if (error == null) {
			//System.out.println(currentTarget + "\t" + targetClass + "\t" + getOutput());
			error = (getTarget() - getOutput()) * activationDerivative();
		}
		return error;
	}
	
	public double getTarget() {
		int target = 0;
		if (currentTarget - targetClass == 0.0) target = 1;
		return target;
	}
	
	public void setCurrentTarget(double target) {
		this.currentTarget = target;
	}
	
	public double getTargetClass() {
		return targetClass;
	}
	
	public void setTargetClass(double target) {
		this.targetClass = target;
	}
}
