package main;

import at.jku.dke.slotmachine.controller.service.dto.FlightListDTO;
import at.jku.dke.slotmachine.controller.service.dto.RegulationNotificationDTO;
import at.jku.dke.slotmachine.controller.service.dto.RegulationRegistrationDTO;
import at.jku.dke.slotmachine.controller.service.dto.WeightMapDTO;
import connectivity.JsonInput;
import connectivity.JsonOutputWriter;
import connectivity.TestDataGeneratorConnector;
import testdatagenerator.TestDataConfigDTO;
import testdatagenerator.TestDataGenerator;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main (String [] args){

        RegulationRegistrationDTO registrationDTO = JsonInput.readRegistration("config/registration.json");
        FlightListDTO flightListDTO = JsonInput.readFlightList("config/flightlist.json");
        RegulationNotificationDTO regulationNotificationDTO = new RegulationNotificationDTO();
        regulationNotificationDTO.setAirlineId("SWR");
        regulationNotificationDTO.setAirportId("LSZH");
        List<WeightMapDTO> weightMapDTOList = new ArrayList<>();
        weightMapDTOList.add(TestDataGeneratorConnector.generateTestdata(flightListDTO, regulationNotificationDTO));
        JsonOutputWriter.writeToFile("./OutputTest.json", weightMapDTOList);
    }
}
