package cooking.neuralnetwork;

public class HiddenNode extends AbstractSigmoidNode {

	private static final long serialVersionUID = 1L;

	public HiddenNode() {
		super();
	}
	
	public HiddenNode(double learningRate) {
		super(learningRate);
	}
	
	public HiddenNode(double learningRate, double momentumTerm) {
		super(learningRate, momentumTerm);
	}
	
	public void weightUpdate(AbstractNode parent) throws Exception {
		double weightChange = learningRate * parent.getError() * this.getOutput();
		
		if (callChildren) {
			for (AbstractNode child : getChildren()) {
				child.weightUpdate(this);
			}
			callChildren = false;
		}
		int parentIndex = parents.indexOf(parent);
		parentWeights.set(parentIndex, weightChange + parentWeights.get(parentIndex));
	}

	public double getError() throws Exception {
		if (error == null) {
			error = 0.0;
			for (int i = 0; i < parents.size(); i++) {
				error += parents.get(i).getError() * parentWeights.get(i);
			}
			error *= activationDerivative();
		}
		return error;
	}
}
