package com.seeat.server.security.oauth2.application.dto;

import com.seeat.server.domain.user.domain.entity.UserSocial;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TempUserInfo {
    private String email;
    private String socialId;
    private UserSocial social;
    private String username;
}
