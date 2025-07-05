package com.seeat.server.security.oauth2.application.dto.response;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getNickname();
    String getProfileImage();
}
