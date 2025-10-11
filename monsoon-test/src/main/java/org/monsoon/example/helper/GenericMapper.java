package org.monsoon.example.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.monsoon.example.dto.UserDto;

public class GenericMapper {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static <T> T map(Object source, Class<T> clazz) {
        if (source == null) return null;
        return mapper.convertValue(source, clazz);
    }
}
