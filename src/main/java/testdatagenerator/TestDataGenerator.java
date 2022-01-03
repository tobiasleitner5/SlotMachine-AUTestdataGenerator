package testdatagenerator;


import at.jku.dke.slotmachine.controller.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import connectivity.JsonOutputWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TestDataGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestDataGenerator.class);
	private static final Random random = new Random();

	private TestDataGenerator(){
		super();
	}

	public static WeightMapElement[] generateTestData(TestDataConfigDTO testDataConfigDTO, FlightListDTO flightListDTO) {
		List<SlotDTO> slotList = flightListDTO.getSlots();
		List<FlightDTO> flightList = flightListDTO.getFlights();
		addMissingScheduledTime(flightList);
		slotList.sort(Comparator.comparing(SlotDTO::getSlotTime));
		flightList.sort(Comparator.comparing(FlightDTO::getScheduledTakeOffTime));
		//checks
		try {
			checkInputs(testDataConfigDTO, flightListDTO, slotList, flightList);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(99);
		}
		LOGGER.info("Generating margins...");
		List<MarginEntry> margins = generateMargins(testDataConfigDTO, slotList, flightList); //while weightMap == null or counter > 3 ... system.exit
		LOGGER.info("Margins created successfully.");
		LOGGER.info("Generating Weight Map...");
		WeightMapElement[] weightMap = generateWeightMapElements(margins, testDataConfigDTO, flightList, slotList);
		JsonOutputWriter.writeToFile("./OutputTest.json", weightMap);
		LOGGER.info("Weight Map created successfully.");
		return weightMap;
	}


	/**
	 * FPFS: For every flight.scheduledTime the difference between all available slottimes is calculated and the nearest slot is assigned to the flights
	 * with usage of the first planned first serve strategy.
	 * @return
	 */
	private static List<MarginEntry> generateMargins(TestDataConfigDTO testDataConfigDTO, List<SlotDTO> slotList, List<FlightDTO> flightList) {
		List<MarginEntry> margins = new ArrayList<>();
		switch (testDataConfigDTO.getMarginDerivation()) {
			case FPFS -> {
				List<SlotDTO> internSlotList = new ArrayList<>(slotList);
				Map<SlotDTO, FlightDTO> mapping = new HashMap<>();
				for (FlightDTO f : flightList) {
					List<Long> diffs = new ArrayList<>();
					for (int i = 0; i < internSlotList.size(); i++) {
						SlotDTO slotDTO = internSlotList.get(i);
						if (slotDTO.getSlotTime().isBefore(f.getScheduledTakeOffTime())) {
							diffs.add(Long.MIN_VALUE);
							continue;
						}
						long minutes = ChronoUnit.MINUTES.between(f.getScheduledTakeOffTime(), slotDTO.getSlotTime());
						diffs.add(minutes);
					}
					//Find minimum diff
					long min = Long.MAX_VALUE;
					int index = 0;
					boolean set = false;
					for (int i = 0; i < diffs.size(); i++) {
						if (diffs.get(i) < min && diffs.get(i) >= 0) { // Hard Constraint: time wished needs to be after scheduled time
							min = diffs.get(i);
							index = i;
							set = true;
						}
					}
					if (!set) {
						LOGGER.error("Not VALID!");
					}
					mapping.put(internSlotList.get(index), f);
					internSlotList.remove(index);
				}
				int i = 0;
				for (Map.Entry<SlotDTO, FlightDTO> entry : mapping.entrySet()) {
					int marginWindow = random.nextInt(testDataConfigDTO.getMaxMarginWindowLength() - testDataConfigDTO.getMinMarginWindowLength() + 1) + testDataConfigDTO.getMinMarginWindowLength();
					String flightId = entry.getValue().getFlightId();
					LocalDateTime scheduledTime = entry.getValue().getScheduledTakeOffTime();
					LocalDateTime timeWished = entry.getKey().getSlotTime();
					LocalDateTime timeNotBefore = timeWished.minusSeconds(marginWindow / 2);
					LocalDateTime timeNotAfter = timeWished.plusSeconds(marginWindow / 2);
					double priority = getCurrentPriority(testDataConfigDTO.getPrioritySettings(), i, flightList.size());
					MarginEntry marginEntry = new MarginEntry(flightId, scheduledTime, timeNotBefore, timeWished, timeNotAfter, priority);
					margins.add(marginEntry);
					i++;
				}
			}
			case RANDOM -> {
				LocalDateTime end = slotList.get(slotList.size()-1).getSlotTime();
				int i = 0;
				for (FlightDTO f : flightList) {
					int marginWindow = random.nextInt(testDataConfigDTO.getMaxMarginWindowLength() - testDataConfigDTO.getMinMarginWindowLength() + 1) + testDataConfigDTO.getMinMarginWindowLength();
					LocalDateTime scheduledTime = f.getScheduledTakeOffTime();
					Duration duration = Duration.between(scheduledTime, end);
					int seconds = (int) duration.getSeconds();
					int randomSeconds = random.nextInt(seconds - 0+ 1) + 0;
					String flightId = f.getFlightId();
					LocalDateTime timeWished = f.getScheduledTakeOffTime().plusSeconds(randomSeconds);
					LocalDateTime timeNotBefore = timeWished.minusSeconds(marginWindow / 2);
					LocalDateTime timeNotAfter = timeWished.plusSeconds(marginWindow / 2);
					double priority = getCurrentPriority(testDataConfigDTO.getPrioritySettings(), i, flightList.size());
					MarginEntry marginEntry = new MarginEntry(flightId, scheduledTime, timeNotBefore, timeWished, timeNotAfter, priority);
					margins.add(marginEntry);
					i++;
				}
			}
		}
		margins.sort(Comparator.comparing(MarginEntry::getTimeWished));
		//Hard Constraint: Time not before must be greater than scheduled time
		for(MarginEntry m :margins){
			if(m.getTimeNotBefore().isBefore(m.getScheduledTime())){
				m.setTimeNotBefore(m.getScheduledTime());
			}
		}
		return margins;
	}

	/**
	 * Due to the problem that there might be flights without a scheduledTakeoffTime I added this method to
	 * set a scheduledTakeoffTime to every flight. The method first checks, whether a TakeoffTime is given or not
	 * and if it is null, the scheduledTakeoffTime is set to the value of the estimatedTakeoffTime.
	 *
	 * Why does every flight need a scheduledTakeoffTime? Because the scheduledTakeoffTime is used to sort the
	 * flightList and the scheduledTakeoffTime is also part of the final WeightMap.
	 */
	private static void addMissingScheduledTime(List<FlightDTO> flightList){
		for(FlightDTO f : flightList){
			if(f.getScheduledTakeOffTime() == null){
				f.setScheduledTakeOffTime(f.getEstimatedTakeOffTime());
			}
		}
	}

	private static double getCurrentPriority(double[][] priority, int i, int size) {
		double currentPriority = 1.0;
		for (int k = 0; k < priority.length; k++) {
			double currentLocation = ((double)(i)/(double)(size)) * 100; // 20 -> at 20%
			if (currentLocation < priority[k][1] && currentLocation >= priority[k][0]) {
				OptionalDouble od = random.doubles(priority[k][2], priority[k][3] + 1E-10).findFirst();
				try {
					currentPriority = od.getAsDouble();
				} catch (Exception e) {
					// if no value is present use lower value
					currentPriority = priority[k][2];
				}
				return currentPriority;
			}

		}
		return currentPriority;
	}

	/**
	 *
	 * @param margins the WeightMap is based on the created Margins.
	 * @return WeigthMap.
	 */
	private static WeightMapElement[] generateWeightMapElements(List<MarginEntry> margins, TestDataConfigDTO tdgConfig, List<FlightDTO> flightList, List<SlotDTO> slotList) {
		// scheduledTime currently set to slotBegin for all flights
		WeightMapElement[] weightMapElements = new WeightMapElement[flightList.size()];

		for (int i = 0; i < flightList.size(); i++) {
			String flightId = flightList.get(i).getFlightId();
			LocalDateTime scheduledTime = flightList.get(i).getScheduledTakeOffTime();
			MarginEntry marginEntry = margins.get(i);
			weightMapElements[i] = generateWeightMapElement(flightId, scheduledTime, marginEntry, slotList, tdgConfig);
		}
		return weightMapElements;
	}

	private static WeightMapElement generateWeightMapElement(String flightId, LocalDateTime scheduledTime, MarginEntry marginEntry, List<SlotDTO> slotList, TestDataConfigDTO tdgConfig){
		WeightMapElementGenerator weightMapElementGenerator = WeightMapElementGeneratorFactory.createWeightMapGenerator(tdgConfig.getWeightMapElementGenerator());
		int[] weightMap = weightMapElementGenerator.generateWeightMap(marginEntry, slotList, tdgConfig, slotList.get(slotList.size()-1).getSlotTime());
		return new WeightMapElement(flightId, scheduledTime.atZone(ZoneId.of("Europe/Vienna")).toInstant(), weightMap);
	}

	private static void checkInputs(TestDataConfigDTO testDataConfigDTO, FlightListDTO flightListDTO, List<SlotDTO> slotList, List<FlightDTO> flightList) throws IOException {
		if(!checkAndReadConfig(testDataConfigDTO)){
			throw new IOException("Config file is not valid.");
		}
		if(flightListDTO.getFlights().size() > flightListDTO.getSlots().size()){
			throw new IOException("FlightList is not valid (More flights than slots).");
		}
		for(int i = 0; i < slotList.size(); i++){
			if(slotList.get(i).getSlotTime().isBefore(flightList.get(0).getScheduledTakeOffTime()) && slotList.size() - flightList.size() < i+1){
				throw new IOException("Not enough possible slots for the flights.");
			}
		}
		LOGGER.error("All inputs are OK.");
	}

	private static boolean checkAndReadConfig(TestDataConfigDTO testDataConfigDTO){
		if(!testDataConfigDTO.getMarginDerivation().equals(MarginDerivation.FPFS) && !testDataConfigDTO.getMarginDerivation().equals(MarginDerivation.RANDOM)){
			LOGGER.error("Distribution Settings: FPFS or RANDOM.");
			return false;
		}
		if(!testDataConfigDTO.getWeightMapElementGenerator().equals("testdatagenerator.WeightMapElementGeneratorLinear")){
			LOGGER.error("Factory Settings: testdatagenerator.WeightMapElementGeneratorLinear supported.");
			return false;
		}
		return true;
	}

}
