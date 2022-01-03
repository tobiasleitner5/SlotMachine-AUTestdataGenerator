package testdatagenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeightMapElementGeneratorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeightMapElementGenerator.class);

    public static WeightMapElementGenerator createWeightMapGenerator(String type){
        switch (type){
            case "testdatagenerator.WeightMapElementGeneratorLinear":
                return new WeightMapElementGeneratorLinear();
        }
        LOGGER.error("fail");
        return null;
    }
}
