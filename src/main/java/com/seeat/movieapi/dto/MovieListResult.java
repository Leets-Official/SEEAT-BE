package com.seeat.movieapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class MovieListResult {
    private int totCnt;
    private List<MovieListDto> movieList;
}

