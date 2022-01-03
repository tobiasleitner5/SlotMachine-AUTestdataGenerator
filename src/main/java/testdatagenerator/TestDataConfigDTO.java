package testdatagenerator;

import java.util.Arrays;

public class TestDataConfigDTO {
	private String weightMapElementGenerator;
	private int maxMarginWindowLength; // in seconds
	private int minMarginWindowLength;
	private String marginDerivation;
	private double[][] prioritySettings;
	private int minValue;
	private int maxValue;
	private int dropValue;

	public String getMarginDerivation() {
		return marginDerivation;
	}

	public void setMarginDerivation(String marginDerivation) {
		this.marginDerivation = marginDerivation;
	}

	public double[][] getPrioritySettings() {
		return prioritySettings;
	}

	public void setPrioritySettings(double[][] prioritySettings) {
		this.prioritySettings = prioritySettings;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getDropValue() {
		return dropValue;
	}

	public void setDropValue(int dropValue) {
		this.dropValue = dropValue;
	}

	public int getMaxMarginWindowLength() {
		return maxMarginWindowLength;
	}

	public void setMaxMarginWindowLength(int maxMarginWindowLength) {
		this.maxMarginWindowLength = maxMarginWindowLength;
	}

	public int getMinMarginWindowLength() {
		return minMarginWindowLength;
	}

	public void setMinMarginWindowLength(int minMarginWindowLength) {
		this.minMarginWindowLength = minMarginWindowLength;
	}

	public String getWeightMapElementGenerator() {
		return weightMapElementGenerator;
	}

	public void setWeightMapElementGenerator(String weightMapElementGenerator) {
		this.weightMapElementGenerator = weightMapElementGenerator;
	}

	public TestDataConfigDTO() {
		super();
	}

	@Override
	public String toString() {
		return "TestDataConfigDTO{" +
				"weightMapElementGenerator='" + weightMapElementGenerator + '\'' +
				", maxMarginWindowLength=" + maxMarginWindowLength +
				", minMarginWindowLength=" + minMarginWindowLength +
				", marginDerivation=" + marginDerivation +
				", prioritySettings=" + Arrays.toString(prioritySettings) +
				", minValue=" + minValue +
				", maxValue=" + maxValue +
				", dropValue=" + dropValue +
				'}';
	}
}
