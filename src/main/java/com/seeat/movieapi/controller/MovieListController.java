package com.seeat.movieapi.controller;

import com.seeat.movieapi.dto.MovieListDto;
import com.seeat.movieapi.service.MovieListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieListController {

    private final MovieListService movieListService;

    @GetMapping
    public ResponseEntity<List<MovieListDto>> getMovieList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<MovieListDto> list = movieListService.fetchMovieList(page, size)
                .getMovieListResult()
                .getMovieList();
        return ResponseEntity.ok(list);
    }
}

