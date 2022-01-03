package testdatagenerator;

import at.jku.dke.slotmachine.controller.service.dto.SlotDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface WeightMapElementGenerator {
    int[] generateWeightMap(MarginEntry margin, List<SlotDTO> sequence, TestDataConfigDTO tdgConfig, LocalDateTime timeSlotEnd);
}
