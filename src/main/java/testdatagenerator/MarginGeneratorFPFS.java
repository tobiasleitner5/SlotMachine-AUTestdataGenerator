package testdatagenerator;

import at.jku.dke.slotmachine.controller.service.dto.FlightDTO;
import at.jku.dke.slotmachine.controller.service.dto.SlotDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MarginGeneratorFPFS implements MarginGenerator{
    private static final Logger LOGGER = LoggerFactory.getLogger(MarginGeneratorFPFS.class);

    @Override
    public List<MarginEntry> generateMargins(TestDataConfigDTO testDataConfigDTO, List<SlotDTO> slotList, List<FlightDTO> flightList, Random random) {
        List<MarginEntry> margins = new ArrayList<>();
            List<SlotDTO> internSlotList = new ArrayList<>(slotList);
            Map<SlotDTO, FlightDTO> mapping = new HashMap<>();
            for (FlightDTO f : flightList) {
                List<Long> diffs = new ArrayList<>();
                for (int i = 0; i < internSlotList.size(); i++) {
                    SlotDTO slotDTO = internSlotList.get(i);
                    if (slotDTO.getSlotTime().isBefore(f.getCalculatedTakeOffTime())) {
                        diffs.add(Long.MIN_VALUE);
                        continue;
                    }
                    long minutes = ChronoUnit.MINUTES.between(f.getCalculatedTakeOffTime(), slotDTO.getSlotTime());
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
                    LOGGER.error("This error should be covered earlier!");
                }
                mapping.put(internSlotList.get(index), f);
                internSlotList.remove(index);
            }
            int i = 0;
            for (Map.Entry<SlotDTO, FlightDTO> entry : mapping.entrySet()) {
                int marginWindow = random.nextInt(testDataConfigDTO.getMaxMarginWindowLength() - testDataConfigDTO.getMinMarginWindowLength() + 1) + testDataConfigDTO.getMinMarginWindowLength();
                String flightId = entry.getValue().getFlightId();
                LocalDateTime scheduledTime = entry.getValue().getCalculatedTakeOffTime();
                LocalDateTime timeWished = entry.getKey().getSlotTime();
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
