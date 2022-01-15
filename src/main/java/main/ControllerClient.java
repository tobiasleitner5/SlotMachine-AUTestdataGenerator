package main;

import at.jku.dke.slotmachine.controller.service.dto.FlightListDTO;
import at.jku.dke.slotmachine.controller.service.dto.RegulationRegistrationDTO;
import at.jku.dke.slotmachine.controller.service.dto.WeightMapDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class ControllerClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String controller = "http://localhost:8090";

    public FlightListDTO getFligthList(String airlineId, String airportId) throws IOException {
        URL url = new URL(controller + "/registrations/" + airlineId + "/" + airportId + "/optimizations/current/flightList");
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        String objectString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        objectMapper.findAndRegisterModules();
        return objectMapper.readValue(objectString, FlightListDTO.class);
    }

}
