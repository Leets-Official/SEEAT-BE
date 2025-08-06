package com.seeat.server.domain.review_ai.external;

public class LangchainApiException extends RuntimeException {
  public LangchainApiException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public LangchainApiException(String message) {
    super(message);

  }
}
