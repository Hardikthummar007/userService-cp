package com.example.userService.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class utility {

    public <T> T convertResponse(ResponseEntity<?> response, TypeReference<T> typeRef) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return objectMapper.convertValue(response.getBody(), typeRef);
    }
}