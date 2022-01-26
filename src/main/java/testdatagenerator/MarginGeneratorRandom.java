package testdatagenerator;

import at.jku.dke.slotmachine.controller.service.dto.FlightDTO;
import at.jku.dke.slotmachine.controller.service.dto.SlotDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class MarginGeneratorRandom implements MarginGenerator{

    @Override
    public List<MarginEntry> generateMargins(TestDataConfigDTO testDataConfigDTO, List<SlotDTO> slotList, List<FlightDTO> flightList, Random random) {
        List<MarginEntry> margins = new ArrayList<>();
        LocalDateTime end = slotList.get(slotList.size()-1).getSlotTime();
        int i = 0;
        for (FlightDTO f : flightList) {
            int marginWindow = random.nextInt(testDataConfigDTO.getMaxMarginWindowLength() - testDataConfigDTO.getMinMarginWindowLength() + 1) + testDataConfigDTO.getMinMarginWindowLength();
            LocalDateTime scheduledTime = f.getCalculatedTakeOffTime();
            Duration duration = Duration.between(scheduledTime, end);
            int seconds = (int) duration.getSeconds();
            int randomSeconds = random.nextInt(seconds + 1);
            String flightId = f.getFlightId();
            LocalDateTime timeWished = f.getCalculatedTakeOffTime().plusSeconds(randomSeconds);
            LocalDateTime timeNotBefore = timeWished.minusSeconds(marginWindow / 2);
            LocalDateTime timeNotAfter = timeWished.plusSeconds(marginWindow / 2);
            double priority = MarginGenerator.getCurrentPriority(testDataConfigDTO.getPrioritySettings(), i, flightList.size(), random);
            MarginEntry marginEntry = new MarginEntry(flightId, scheduledTime, timeNotBefore, timeWished, timeNotAfter, priority);
            margins.add(marginEntry);
            i++;
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
}
