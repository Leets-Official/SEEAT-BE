package com.seeat.server.domain.user.presentation.swagger;

import com.seeat.server.domain.user.application.dto.UserSignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface UserControllerSpec {

    @PostMapping
    @Operation(summary = "Sign up", description = "Additional sign-up after first-time login")
    ResponseEntity<?> userSignUp(
            @RequestBody UserSignUpRequest request,
            @RequestHeader String tempUserKey
    );

    @PostMapping("/logout")
    @Operation(summary = "Log out", description = "Logs out user and deletes the refresh token")
    ResponseEntity<?> userLogout(HttpServletRequest request, HttpServletResponse response);
}
