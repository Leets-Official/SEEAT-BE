package com.seeat.movieapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MovieListResponse {
    @JsonProperty("movieListResult")
    private MovieListResult movieListResult;
}
