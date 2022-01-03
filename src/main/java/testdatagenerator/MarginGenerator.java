package testdatagenerator;

import at.jku.dke.slotmachine.controller.service.dto.FlightDTO;
import at.jku.dke.slotmachine.controller.service.dto.SlotDTO;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;

public interface MarginGenerator {
    List<MarginEntry> generateMargins(TestDataConfigDTO testDataConfigDTO, List<SlotDTO> slotList, List<FlightDTO> flightList, Random random);

    static double getCurrentPriority(double[][] priority, int i, int size, Random random) {
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
}
