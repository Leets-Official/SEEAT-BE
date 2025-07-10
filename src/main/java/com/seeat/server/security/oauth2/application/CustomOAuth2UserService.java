package com.seeat.server.security.oauth2.application;

import com.seeat.server.domain.user.application.UserService;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.KakaoUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.NaverUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
/**
 * OAuth2 사용자 정보를 처리하는 서비스
 *
 * - 소셜 ID로 기존 사용자 조회
 * - 이메일 중복 체크
 * - 신규 사용자 정보 반환
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = getUserInfo(registrationId, oAuth2User.getAttributes());

        UserSocial social = UserSocial.valueOf(registrationId.toUpperCase());
        String socialId = userInfo.getProviderId();
        String email = userInfo.getEmail();

        Optional<User> userOpt = userService.getUserBySocialAndSocialId(social, socialId);

        if (userOpt.isPresent()) {
            return CustomUserInfo.ofExistingUser(userOpt.get(), oAuth2User.getAttributes());
        }

        Optional<User> userByEmail = userService.getUserByEmail(email);

        if (userByEmail.isPresent()) {
            return CustomUserInfo.ofEmailDuplicate(userInfo, social, oAuth2User.getAttributes());
        }

        return CustomUserInfo.ofNewUser(userInfo, social, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo getUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> new KakaoUserInfo(attributes);
            case "naver" -> new NaverUserInfo(attributes);

            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN, null);
        };
    }
}
