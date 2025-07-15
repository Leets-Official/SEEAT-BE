package com.seeat.movieapi.service;

import com.seeat.movieapi.dto.MovieInfoResponse;
import com.seeat.movieapi.dto.MovieInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MovieDetailService {

    @Value("${kobis.key}")
    private String kobisKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public MovieInfo fetchMovieDetail(String movieCd) {
        String url = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json"
                + "?key=" + kobisKey
                + "&movieCd=" + movieCd;

        ResponseEntity<MovieInfoResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody()
                    .getMovieInfoResult()
                    .getMovieInfo();
        } else {
            throw new RuntimeException("KOBIS API 호출 실패: " + response.getStatusCode());
        }
    }
}
