package testdatagenerator;

import java.time.Instant;
import java.util.Arrays;

public class WeightMapElement {
    private String flightId;
    private Instant scheduledTime;
    private int[] weightMap;

    public WeightMapElement(String flightId, Instant scheduledTime, int[] weightMap) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.weightMap = weightMap;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public int[] getWeightMap() {
        return weightMap;
    }

    public void setWeightMap(int[] weightMap) {
        this.weightMap = weightMap;
    }

    @Override
    public String toString() {
        return "WeightMapElement{" +
                "flightId='" + flightId + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", weightMap=" + Arrays.toString(weightMap) +
                '}';
    }
}
