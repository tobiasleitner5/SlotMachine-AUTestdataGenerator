package testdatagenerator;


import at.jku.dke.slotmachine.controller.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import connectivity.JsonOutputWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class TestDataGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestDataGenerator.class);
	private static final Random random = new Random();

	private TestDataGenerator(){
		super();
	}

	public static WeightMapElement[] generateTestData(TestDataConfigDTO testDataConfigDTO, FlightListDTO flightListDTO) throws IOException {
		List<SlotDTO> slotList = flightListDTO.getSlots();
		List<FlightDTO> flightList = flightListDTO.getFlights();
		addMissingScheduledTime(flightList);
		slotList.sort(Comparator.comparing(SlotDTO::getSlotTime));
		flightList.sort(Comparator.comparing(FlightDTO::getScheduledTakeOffTime));
		// Checks
		checkInputs(testDataConfigDTO, flightListDTO, slotList, flightList);
		LOGGER.info("Generating margins...");
		MarginGenerator marginGenerator = MarginGeneratorFactory.createMarginGenerator(testDataConfigDTO.getMarginDerivation());
		List<MarginEntry> margins = marginGenerator.generateMargins(testDataConfigDTO, slotList, flightList, random); //while weightMap == null or counter > 3 ... system.exit
		LOGGER.info("Margins created successfully.");
		LOGGER.info("Generating Weight Map...");
		WeightMapElement[] weightMap = generateWeightMapElements(margins, testDataConfigDTO, flightList, slotList);
		LOGGER.info("Weight Map created successfully.");
		return weightMap;
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
		LOGGER.info("All inputs are OK.");
	}

	private static boolean checkAndReadConfig(TestDataConfigDTO testDataConfigDTO){
		if(!testDataConfigDTO.getMarginDerivation().equals("testdatagenerator.MarginGeneratorFPFS") && !testDataConfigDTO.getMarginDerivation().equals("testdatagenerator.MarginGeneratorRandom")){
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
