package cooking.evaluation;

public class Evaluation {
	private double numericalEvaluation;
	private String stringEvaluation;
	
	// Assumes string evaluation is of the format "words " so that the value can be appended without issue
	public Evaluation(double numericalEvaluation, String stringEvaluation) {
		this.setNumericalEvaluation(numericalEvaluation);
		this.setStringEvaluation(stringEvaluation);
	}

	public void setNumericalEvaluation(double numericalEvaluation) {
		this.numericalEvaluation = numericalEvaluation;
	}

	public double getNumericalEvaluation() {
		return numericalEvaluation;
	}

	public void setStringEvaluation(String stringEvaluation) {
		this.stringEvaluation = stringEvaluation;
	}

	public String getStringEvaluation() {
		return stringEvaluation + numericalEvaluation;
	}
}
