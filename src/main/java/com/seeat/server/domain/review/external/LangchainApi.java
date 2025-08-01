package com.seeat.server.domain.review.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

/**
 * Langchain API 외부 연동을 담당하는 컴포넌트입니다.
 * WebClient를 활용하여 Langchain 요약 API에 비동기 방식으로 요청을 보내고,
 * 응답 결과를 Mono<String>으로 반환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LangchainApi {

    private final WebClient webClient;

    /// 요청보낼 값
    @Value("${langchain.query}")
    private String query;

    /// 기본 주소 저장
    @Value("${langchain.url}")
    private String baseUrl;

    /**
     * Langchain 요약 API를 호출하여 auditoriumId에 해당하는 요약 텍스트를 가져옵니다.
     *
     * @param auditoriumId 요약할 상영관 ID
     * @return 요약 결과 문자열을 포함하는 Mono
     */
    public Mono<String> postSummaryByLangchain(String auditoriumId) {

        /// 요청보낼 내용 저장
        Map<String, String> requestBody = Map.of(
                "request", auditoriumId + query
        );

        /// URI 만들기
        String uri = UriComponentsBuilder.fromUri(URI.create(baseUrl))
                .path("/api/v1/summary")
                .toUriString();

        return webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)

                /// 예외 터지면 로그만 찍어두기
                .onErrorResume(throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        int status = ex.getStatusCode().value();
                        String responseBody = ex.getResponseBodyAsString();

                        log.info("LangChain API 오류 발생: status={}, body={}", status, responseBody);

                        String msg;
                        if (status >= 400 && status < 500) {
                            msg = "LangChain 클라이언트 오류 (" + status + "): " + responseBody;
                        } else if (status >= 500) {
                            msg = "LangChain 서버 오류 (" + status + "): " + responseBody;
                        } else {
                            msg = "LangChain 알 수 없는 오류 (" + status + "): " + responseBody;
                        }

                        return Mono.error(new LangchainApiException(msg, throwable));
                    }

                    return Mono.error(new LangchainApiException("LangChain API 통신 실패", throwable));
                });
    }

}
