package connectivity;

import at.jku.dke.slotmachine.controller.service.dto.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testdatagenerator.TestDataConfigDTO;

public class JsonInput {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonInput.class);

    private JsonInput(){
        super();
    }

    public static TestDataConfigDTO readConfig(String fileName) {
        LOGGER.info(String.format("Reading JSON file from '%s'.", fileName));
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        try {
            TestDataConfigDTO testDataConfigDTO  = mapper.readValue(new File(fileName), TestDataConfigDTO.class);
            LOGGER.info("Testdata Config successfully created from JSON file.");
            return testDataConfigDTO;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RegulationRegistrationDTO readRegistration(String fileName) {
        LOGGER.info(String.format("Reading JSON file from '%s'.", fileName));
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        try {
            RegulationRegistrationDTO regulationRegistrationDTO = mapper.readValue(new File(fileName), RegulationRegistrationDTO.class);
            LOGGER.info("RegulationRegistrationDTO successfully created from JSON file.");
            return regulationRegistrationDTO;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static FlightListDTO readFlightList(String fileName) {
        LOGGER.info("Reading JSON file from '" + fileName + "'.");
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        try {
            FlightListDTO flightListDTO = mapper.readValue(new File(fileName), FlightListDTO.class);
            LOGGER.info("RegulationRegistrationDTO sucessfully created from JSON file.");
            return flightListDTO;
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
