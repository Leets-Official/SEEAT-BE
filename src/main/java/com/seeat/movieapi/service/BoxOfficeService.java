package com.seeat.movieapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
public class BoxOfficeService {

    @Value("${kobis.key}")
    private String kobisKey;

    public String fetchBoxOffice(String targetDt) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(
                "https://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=%s&targetDt=%s",
                kobisKey, targetDt
        );
        return restTemplate.getForObject(url, String.class);
    }
}
