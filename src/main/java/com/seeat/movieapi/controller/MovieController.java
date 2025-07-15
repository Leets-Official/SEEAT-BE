package com.seeat.movieapi.controller;

import com.seeat.movieapi.dto.MovieInfo;
import com.seeat.movieapi.service.MovieDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieDetailService movieDetailService;

    public MovieController(MovieDetailService movieDetailService) {
        this.movieDetailService = movieDetailService;
    }

    @GetMapping("/{movieCd}")
    public ResponseEntity<MovieInfo> getMovieDetail(@PathVariable String movieCd) {
        MovieInfo movieInfo = movieDetailService.fetchMovieDetail(movieCd);
        return ResponseEntity.ok(movieInfo);
    }
}