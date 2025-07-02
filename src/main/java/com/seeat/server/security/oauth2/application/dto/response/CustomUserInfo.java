package com.seeat.server.security.oauth2.application.dto.response;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomUserInfo implements OAuth2User {

    public enum UserStatus {
        EXISTING_USER,
        NEW_USER,
        EMAIL_DUPLICATE
    }

    private final UserStatus status;
    private final User user;
    private final OAuth2UserInfo tempUserInfo;
    private final UserSocial social;
    private final Map<String, Object> attributes;

    public CustomUserInfo(User user, Map<String, Object> attributes) {
        this.status = UserStatus.EXISTING_USER;
        this.user = user;
        this.tempUserInfo = null;
        this.social = user.getSocial();
        this.attributes = attributes;
    }

    private CustomUserInfo(UserStatus status, User user, OAuth2UserInfo tempUserInfo,
                           UserSocial social, Map<String, Object> attributes) {
        this.status = status;
        this.user = user;
        this.tempUserInfo = tempUserInfo;
        this.social = social;
        this.attributes = attributes;
    }

    public static CustomUserInfo ofExistingUser(User user, Map<String, Object> attributes) {
        return new CustomUserInfo(user, attributes);
    }

    public static CustomUserInfo ofNewUser(OAuth2UserInfo userInfo, UserSocial social, Map<String, Object> attributes) {
        return new CustomUserInfo(
                UserStatus.NEW_USER,
                null,
                userInfo,
                social,
                attributes
        );
    }

    public static CustomUserInfo ofEmailDuplicate(OAuth2UserInfo userInfo, UserSocial attemptedSocial,
                                                  Map<String, Object> attributes) {
        return new CustomUserInfo(
                UserStatus.EMAIL_DUPLICATE,
                null,
                userInfo,
                attemptedSocial,
                attributes
        );
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        if (this.user == null) {
            Object response = attributes.get("response");
            if (response instanceof Map) {
                Object id = ((Map<?, ?>) response).get("id");
                if (id != null) {
                    return String.valueOf(id);
                }
            }
            Object id = attributes.get("id");
            if (id != null) {
                return String.valueOf(id);
            }
            return null;
        }
        return this.user.getSocialId();
    }

    public User getUser() {
        return user;
    }

    public UserStatus getStatus() {return this.status;}

    public OAuth2UserInfo getTempUserInfo() {return tempUserInfo;}

    public UserSocial getSocial() {return social;}
}
