package com.seeat.movieapi.controller;

import com.seeat.movieapi.service.BoxOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxoffice")
public class BoxOfficeController {

    private final BoxOfficeService boxOfficeService;

    @GetMapping
    public ResponseEntity<String> getBoxOffice(@RequestParam String date) {
        return ResponseEntity.ok(boxOfficeService.fetchBoxOffice(date));
    }
}


