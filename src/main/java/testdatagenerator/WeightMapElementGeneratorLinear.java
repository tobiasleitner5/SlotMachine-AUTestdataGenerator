package testdatagenerator;

import at.jku.dke.slotmachine.controller.service.dto.SlotDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class WeightMapElementGeneratorLinear implements WeightMapElementGenerator{

	private static final Logger LOGGER = LoggerFactory.getLogger(TestDataGenerator.class);

	/**
	 * Generates the weight maps according to the margins, min-/max-values and priority-value.
	 * @param margin contains information about the margins per flight
	 * @param sequence contains information about the possible slots
	 * @return integer array with the generated weight map
	 */
	@Override
	public int[] generateWeightMap(MarginEntry margin, List<SlotDTO> sequence, TestDataConfigDTO tdgConfig, LocalDateTime timeSlotEnd) {
		List<Double> weightMapList = new LinkedList<Double>();
		
		LocalDateTime timeSlotBegin = sequence.get(0).getSlotTime();
		
		// calculate time durations between slot-begin-time and scheduledTime/TimeNotBefore/...
		// value can be negative or positive, Math.abs is not useful here with this cost function
		int intScheduledTime = (int) Duration.between(timeSlotBegin, margin.getScheduledTime()).toSeconds();
		if (intScheduledTime >= 0) { //for current requirements
			intScheduledTime = 0;
		}
		int intTimeNotBefore = (int) Duration.between(timeSlotBegin, margin.getTimeNotBefore()).toSeconds();
		if (intTimeNotBefore < 0) {
			intTimeNotBefore = 0;
		}
		int intTimeNotAfter = (int) Duration.between(timeSlotBegin, margin.getTimeNotAfter()).toSeconds();
		if (intTimeNotAfter < 0) {
			intTimeNotAfter = 0;
		}
		int intTimeWished = (int) Duration.between(timeSlotBegin, margin.getTimeWished()).toSeconds();
		if (intTimeWished < 0) {
			intTimeWished = 0;
		}
		int intSlotBegin = (int) Duration.between(timeSlotBegin, timeSlotBegin).toSeconds(); // always 0
		int intSlotEnd = (int) Duration.between(timeSlotBegin, timeSlotEnd).toSeconds();
		
		if (margin.getTimeNotBefore().isAfter(margin.getTimeWished()) 
				|| margin.getTimeWished().isAfter(margin.getTimeNotAfter())) {
			LOGGER.info("TimeNotBefore must be before TimeWished and/or TimeWished must be before TimeNotAfter! " +
					"Therefore, Weight Map for flight " + margin.getFlightId() + " could have wrong values!");
		}
		
		for (SlotDTO s: sequence) {
			double x = Math.abs(Duration.between(timeSlotBegin, s.getSlotTime()).toSeconds());
			
			// before ScheduledTime, currently unused
			if (s.getSlotTime().isBefore(margin.getScheduledTime())) {
				double min = tdgConfig.getMinValue();
				weightMapList.add(min);
			// between ScheduledTime/SlotBegin (incl.) and TimeNotBefore (excl.)
			//		value at ScheduledTime or SlotStart = minValue
			//		value at TimeNotBefore = 0
			} else if (s.getSlotTime().isBefore(margin.getTimeNotBefore())
					&& !(s.getSlotTime().isBefore(margin.getScheduledTime())) ||
				(s.getSlotTime().isBefore(margin.getTimeNotBefore()) && intScheduledTime == 0)) {
				double currentWeight;
				// f(x) = k*x + minValue - k
				// k = (0 - minValue)/(TimeNotBefore-ScheduledTime) ODER (0 - minValue)/(TimeNotBefore - SlotBegin)
				double k = ((0 - (double)tdgConfig.getMinValue())/(intTimeNotBefore - 0));
				if (intScheduledTime > 0) { //if ScheduledTime is after SlotBegin
					k = (Math.abs((double)tdgConfig.getMinValue())/(intTimeNotBefore - 0));
				}
				currentWeight = k * x + tdgConfig.getMinValue() - k;
				if (currentWeight < (double)tdgConfig.getMinValue()) {
					currentWeight = (double)tdgConfig.getMinValue();
				}
				weightMapList.add(currentWeight);
			// between TimeNotBefore (incl.) and TimeWished (excl.)
			//		value at TimeNotBefore = dropValue
			//		value at TimeNotBefore = maxValue
			} else if (s.getSlotTime().isBefore(margin.getTimeWished())
					&& !(s.getSlotTime().isBefore(margin.getTimeNotBefore()))) {
				double currentWeight = tdgConfig.getMinValue();
				// f(x) = k*x + d
				// k = (maxValue - dropValue)/(TimeWished - TimeNotBefore)
				double k = ((double) tdgConfig.getMaxValue() - (double) tdgConfig.getDropValue())/(intTimeWished - intTimeNotBefore);
				currentWeight = (k * x) + (double) tdgConfig.getDropValue() - (k * intTimeNotBefore);
				weightMapList.add(currentWeight);
			// between TimeWished (incl.) and TimeNotAfter (incl.)
			//		value at TimeWished = maxValue
			//		value at TimeNotAfter = dropValue
			} else if (s.getSlotTime().isBefore(margin.getTimeNotAfter())
					&& !(s.getSlotTime().isBefore(margin.getTimeWished()))) {
				double currentWeight = tdgConfig.getMinValue();
				// f(x) = k*x + d
				// k = (dropValue - maxValue)/(TimeNotAfter - TimeWished)
				double k = ((double) tdgConfig.getDropValue() - (double) tdgConfig.getMaxValue())/(intTimeNotAfter - intTimeWished);
				currentWeight = (k * x) + (double) tdgConfig.getMaxValue() - (k * intTimeWished);
				weightMapList.add(currentWeight);
			// between TimeNotAfter (incl.) and SlotEnd (incl.)
			//		value at TimeNotAfter = 0
			//		value at SlotEnd = minValue
			} else if (!(s.getSlotTime().isBefore(margin.getTimeNotAfter()))) {
				double currentWeight = tdgConfig.getMinValue();
				// f(x) = k*x + d
				// k = (minValue - 0)/(SlotEnd - TimeNotAfter)
				double k = ((double) tdgConfig.getMinValue())/(intSlotEnd - intTimeNotAfter);
				currentWeight = (k * x) + (double) tdgConfig.getMinValue() - (k * intSlotEnd);
				if (currentWeight < (double) tdgConfig.getMinValue()) {
					currentWeight = (double) tdgConfig.getMinValue();
				}
				weightMapList.add(currentWeight);
			} else {
				double minValue = (double) tdgConfig.getMinValue();
				weightMapList.add(minValue);
			}
		}
		
		// convert used list to an array and multiply values by priority
		int[] weightMap = new int[weightMapList.size()];
		int i = 0;
		for (double value: weightMapList) {
			if (margin.getPriority() != 1) {
				weightMap[i] = (int) (value * margin.getPriority());
			} else {
				weightMap[i] = (int) value;
			}
			i++;
		}
		
		return weightMap;
	}
	

}
