package connectivity;

import at.jku.dke.slotmachine.controller.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testdatagenerator.TestDataConfigDTO;
import testdatagenerator.TestDataGenerator;
import testdatagenerator.WeightMapElement;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestDataGeneratorConnector {

    private static final TestDataConfigDTO testDataConfigDTO = JsonInput.readConfig("config/config.json");
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataGeneratorConnector.class);

    private TestDataGeneratorConnector(){
        super();
    }

    /**
     * Takes my weightMaps and creates WeightMapDTO Objects. The reason for that is, that at the moment the classes are often changed. So I
     * decided to create a class that can be easily adapted if the classes are changed again.
     */
    public static WeightMapDTO generateTestdata(FlightListDTO flightListDTO, RegulationNotificationDTO regulationNotificationDTO){
        List<SlotDTO> slotList = flightListDTO.getSlots();
        slotList.sort(Comparator.comparing(SlotDTO::getSlotTime));
        WeightMapElement[] weightMapElements = new WeightMapElement[0];
        try {
            weightMapElements = TestDataGenerator.generateTestData(testDataConfigDTO, flightListDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //print
        try {
            FileWriter weightMapWriter = new FileWriter("weightMap.csv");
            weightMapWriter.write(";");
            for(SlotDTO s : slotList){
                weightMapWriter.write(String.valueOf(s.getSlotTime()) + ";");
            }
            weightMapWriter.write("\n");
            for(WeightMapElement e : weightMapElements){
                weightMapWriter.write(e.getFlightId() + ";");
                for(int i : e.getWeightMap()){
                    weightMapWriter.write(i + ";");
                }
                weightMapWriter.write("\n");
            }
            weightMapWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WeightMapDTO weightMapDTO = new WeightMapDTO();
        List<WeightMapFlightDTO> weightMapFlightDTOList = new ArrayList<>();
        for(WeightMapElement w : weightMapElements){
            WeightMapFlightDTO weightMapFlightDTO = new WeightMapFlightDTO();
            weightMapFlightDTO.setFlightId(w.getFlightId());
            for(int i = 0; i < w.getWeightMap().length; i++){
                weightMapFlightDTO.addWeightMapObject(new WeightMapObjectDTO(slotList.get(i).getSlotTime(), Integer.toString(w.getWeightMap()[i])));
            }
            weightMapFlightDTOList.add(weightMapFlightDTO);
        }
        weightMapDTO.setAirlineId(regulationNotificationDTO.getAirlineId());
        weightMapDTO.setOptimizationId(flightListDTO.getOptimization().getOptimizationId());
        weightMapDTO.setRegulationId(flightListDTO.getOptimization().getRegulationId());
        weightMapDTO.setFlights(weightMapFlightDTOList);
        return weightMapDTO;
    }

}
