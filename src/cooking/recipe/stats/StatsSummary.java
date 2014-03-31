package cooking.recipe.stats;

public class StatsSummary {
	
	public StatsSummary() {
		mean = 0;
		max = 0;
		min = 0;
		variance = 0;
		probabilityOfOccurrence = 0;
		count = 0;
	}
	
	public StatsSummary(double amount) {
		initializeFirst(amount);
	}
	
	private void initializeFirst(double amount) {
		mean = amount;
		max = amount;
		min = amount;
		variance = 0;
		probabilityOfOccurrence = -1;
		count = 1;
	}
	
	private void initializeByCopying(StatsSummary other) {
		setMean(other.getMean());
		setMax(other.getMax());
		setMin(other.getMin());
		setVariance(other.getVariance());
		setProbabilityOfOccurrence(other.getProbabilityOfOccurrence());
		setCount(other.getCount());
	}
	
	private double mean;
	private double max;
	private double min;
	private double variance;
	private double probabilityOfOccurrence;
	private int count;
	
	public void setMean(double mean) {
		this.mean = mean;
	}
	public double getMean() {
		return mean;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public double getMax() {
		return max;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMin() {
		return min;
	}
	public void setVariance(double variance) {
		this.variance = variance;
	}
	public double getVariance() {
		return variance;
	}
	public void setProbabilityOfOccurrence(double probabilityOfOccurrence) {
		this.probabilityOfOccurrence = probabilityOfOccurrence;
	}
	public double getProbabilityOfOccurrence() {
		return probabilityOfOccurrence;
	}

	
	public double getStandardDeviation() {
		return Math.sqrt(getVariance());
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getCount() {
		return count;
	}

	public void UpdateWithNewValue(double amount) {
		if (getCount() < 1) {
			this.initializeFirst(amount);
		}
		else {
			if (amount > getMax()) {
				setMax(amount);
			}
			
			if (amount < getMin()) {
				setMin(amount);
			}
			
			// Note that the count must be updated before the incremental calculations
			count++;
			
			// incremental updates, source: http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance - scroll to on-line algorithm
			double oldMean = getMean();
			double newMean = oldMean + ((amount - oldMean) / count);
			setMean(newMean);
			
			double oldVariance = getVariance();

			// this one does the population variance
			//double newVariance = ((count - 1) * oldVariance + (amount - newMean) * (amount - oldMean)) / count;
			
			// this one does the sample variance
			double newVariance = ((count - 2) * oldVariance + (amount - newMean) * (amount - oldMean)) / (count - 1);
			setVariance(newVariance);
		
		}
		
	}
	
	
	/**
	 * Adds the values stored in the other stats summary into this one.
	 * 
	 * @param other
	 */
	public void UpdateWithSummary(StatsSummary other) {
		if (other.getCount() > 0) {
			// if the other count is less than 0, there is no need to update anything
			
			if (getCount() < 1) {
				// if the current object hasn't been initialized, just copy the other one
				this.initializeByCopying(other);
			}
			else {
				// merge the two sets of values
				
				if (other.getMax() > getMax()) {
					setMax(other.getMax());
				}
				
				if (other.getMin() < getMin()) {
					setMin(other.getMin());
				}
				
				
				double mean1 = getMean();
				double mean2 = other.getMean();
				int count1 = getCount();
				int count2 = other.getCount();
				
				double variance1 = getVariance();
				double variance2 = other.getVariance();
								
				double sum1 = count1 * mean1;
				double sum2 = count2 * mean2;
				
				int newCount = count1 + count2;
				double newMean = (sum1 + sum2) / newCount;
				
				// source: http://en.wikipedia.org/wiki/Standard_deviation#Combining_standard_deviations Scroll to section 8
				double newVariance = (1 / (double)(newCount - 1)) * ((count1 - 1) * variance1 + count1 * Math.pow(mean1, 2) + (count2 - 1) * variance2 + count2 * Math.pow(mean2, 2) - newCount * Math.pow(newMean, 2));
				
				setCount(newCount);
				setMean(newMean);
				setVariance(newVariance);
				
				// assumes that both recipes have the same total population (ie, the same number of recipes in the database) to produce their probability of occurrence
				// assumes that ingredients cannot belong to multiple groups
				setProbabilityOfOccurrence(getProbabilityOfOccurrence() + other.getProbabilityOfOccurrence());
			}
			
		}
	}

	public final int INTERPOLATION_MAX = 100; 
	
	/**
	 * Linearly interpolates this stats object with the passed paramter.
	 * If this object has a count of 0, the other will be used as is.
	 * If this object has a count greater than INTERPOLATION_MAX, the first object will be used as is.
	 * 
	 * Please note that items such as variance are simply linearly interpolated, no effort is made to try to
	 * count both objects as is if they come from the same sample.
	 * 
	 * @param other
	 * @return
	 */
	public StatsSummary interpolateWith(StatsSummary other) {
		StatsSummary combined = new StatsSummary();

		if (getCount() == 0) {
			combined = other.clone();
		}
		else if (getCount() > INTERPOLATION_MAX) {
			combined = this.clone();
		}
		else {
			
			int sum = this.getCount() + other.getCount();
			double myWeight = ((double) this.getCount()) / sum;
			double otherWeight = ((double) other.getCount()) / sum;
			
			combined.mean = myWeight * this.getMean() + otherWeight * other.getMean();
			combined.max = myWeight * this.getMax() + otherWeight * other.getMax();
			combined.min = myWeight * this.getMin() + otherWeight * other.getMin();
			combined.variance = myWeight * this.getVariance() + otherWeight * other.getVariance();
			combined.probabilityOfOccurrence = myWeight * this.getProbabilityOfOccurrence() + otherWeight * other.getProbabilityOfOccurrence();
			combined.count = (int)(myWeight * this.getCount() + otherWeight * other.getCount());
		}
		
		return combined;
	}
	
	@Override
	public StatsSummary clone() {
		StatsSummary clone = new StatsSummary();
		
		clone.mean = this.mean;
		clone.max = this.max;
		clone.min = this.min;
		clone.variance = this.variance;
		clone.probabilityOfOccurrence = this.probabilityOfOccurrence;
		clone.count = this.count;
		
		return clone;
	}
}
