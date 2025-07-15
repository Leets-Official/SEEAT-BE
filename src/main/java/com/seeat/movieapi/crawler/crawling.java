package com.seeat.movieapi.crawler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.movieapi.dto.Cinema;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class crawling {
    public static void main(String[] args) {
        try {
            // 1. JSON 파일 로드
            InputStream is = crawling.class.getClassLoader().getResourceAsStream("data/cinemas.json");
            if (is == null) throw new RuntimeException("cinemas.json 파일이 없습니다");

            ObjectMapper mapper = new ObjectMapper();
            List<Cinema> cinemaList = mapper.readValue(is, new TypeReference<>() {});

            // 2. 공통 정보
            String url = "https://www.lottecinema.co.kr/LCWS/Ticketing/TicketingData.aspx";
            HttpClient client = HttpClient.newHttpClient();

            // 3. 반복 요청
            for (Cinema cinema : cinemaList) {
                String jsonPayload = String.format("""
                    {
                      "MethodName": "GetSeats",
                      "channelType": "HO",
                      "osType": "W",
                      "osVersion": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                      "cinemaId": "%s",
                      "screenId": "%s",
                      "playDate": "%s",
                      "playSequence": "%s",
                      "screenDivisionCode": "%s"
                    }
                    """, cinema.getCinemaId(), cinema.getScreenId(), cinema.getPlayDate(),
                        cinema.getPlaySequence(), cinema.getScreenDivisionCode());

                String formEncoded = "paramList=" + URLEncoder.encode(jsonPayload, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("User-Agent", "Mozilla/5.0")
                        .POST(HttpRequest.BodyPublishers.ofString(formEncoded))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("[" + cinema.getCinemaId() + "] 응답 코드: " + response.statusCode());
                System.out.println(response.body());
                System.out.println("--------------------------------------------------");
            }

        } catch (Exception e) {
            System.err.println("❌ 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
