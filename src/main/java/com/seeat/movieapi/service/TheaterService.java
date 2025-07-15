package com.seeat.movieapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.movieapi.dto.TheaterDto;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class TheaterService {

    public List<TheaterDto> loadTheaterData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/data/경기도영화상영관현황.json");

            TheaterDto[] dtos = objectMapper.readValue(is, TheaterDto[].class);
            return Arrays.asList(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
