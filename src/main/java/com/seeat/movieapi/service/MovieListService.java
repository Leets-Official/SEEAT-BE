package com.seeat.movieapi.service;

import com.seeat.movieapi.dto.MovieListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class MovieListService {

    @Value("${kobis.key}")
    private String kobisKey;

    public MovieListResponse fetchMovieList(int page, int size) {
        String url = String.format(
                "https://kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieList.json?key=%s&curPage=%d&itemPerPage=%d",
                kobisKey, page, size
        );

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, MovieListResponse.class);
    }
}
