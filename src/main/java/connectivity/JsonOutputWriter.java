package connectivity;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Only for testing purposes.
 */

public class JsonOutputWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonOutputWriter.class);

    /**
     * Writes content of object to file
     * @param fileName name of file, which is created/updated
     * @param object object, which is used as the content for the file
     */
    public static void writeToFile(String fileName, Object object) {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        try {
            File file = new File(fileName);
            writer.writeValue(file, object);
            LOGGER.info("Writing information to file '" + fileName + "'.");
        } catch (IOException e) {
            LOGGER.error("Cannot write JSON to file '" + fileName + "'.", e);
        }
    }
}
