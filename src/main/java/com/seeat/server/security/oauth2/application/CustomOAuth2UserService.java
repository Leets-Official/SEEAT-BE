package com.seeat.server.security.oauth2.application;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.entity.UserRole;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.KakaoUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.NaverUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo;
        if ("kakao".equals(registrationId)) {
            userInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else if ("naver".equals(registrationId)) {
            userInfo = new NaverUserInfo(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2");
        }

        Optional<User> userOpt = userRepository.findBySocialAndSocialId(
                UserSocial.valueOf(registrationId.toUpperCase()), userInfo.getProviderId());

        if (userOpt.isEmpty()) {
            throw new OAuth2AuthenticationException("SIGNUP_REQUIRED:" + userInfo.getProviderId());
        }

        User user = userOpt.get();

        return new CustomUserInfo(user, oAuth2User.getAttributes());

    }
}
