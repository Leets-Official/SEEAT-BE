package com.seeat.movieapi.controller;

import com.seeat.movieapi.dto.TheaterDto;
import com.seeat.movieapi.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    @GetMapping
    public ResponseEntity<List<TheaterDto>> getTheaters() {
        return ResponseEntity.ok(theaterService.loadTheaterData());
    }
}
