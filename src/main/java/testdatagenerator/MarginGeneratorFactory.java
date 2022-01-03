package testdatagenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarginGeneratorFactory {

    private MarginGeneratorFactory(){
        super();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MarginGeneratorFactory.class);

    public static MarginGenerator createMarginGenerator(String type){
        switch (type){
            case "testdatagenerator.MarginGeneratorFPFS":
                return new MarginGeneratorFPFS();
            case "testdatagenerator.MarginGeneratorRandom":
                return new MarginGeneratorRandom();
        }
        LOGGER.error("fail");
        return null;
    }
}
