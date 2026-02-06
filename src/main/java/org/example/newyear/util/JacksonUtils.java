package org.example.newyear.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JacksonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T convert(JsonNode json, Class<T> clazz) {
        return mapper.convertValue(json, clazz);
    }
}
