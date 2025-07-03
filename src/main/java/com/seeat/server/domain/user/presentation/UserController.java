package com.seeat.server.domain.user.presentation;


import com.seeat.server.domain.user.application.UserService;
import com.seeat.server.domain.user.application.dto.UserSignUpRequest;
import com.seeat.server.domain.user.presentation.swagger.UserControllerSpec;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerSpec {

    private final RedisService redisService;
    private final UserService userService;

    /**
     *최초 로그인시 추가 회원가입을 진행합니다.
     *
     * @param request 추가 정보 요청값 (닉네임, 유저프로필, 선호 장르, 선호 극장)
     * @param tempUserKey 임시유저정보 담긴 RedisKey
     * @return 회원가입 완료 응답
     */
    @PostMapping
    public ResponseEntity<?> userSignUp(@RequestBody UserSignUpRequest request, //HttpSession session
                                        @RequestHeader("Temp-User-Key") String tempUserKey) {
        /* redirect페이지가 아직 404라서 세션이 안됨 주석처리 해둠 => 세션으로 넣을 예정
        String tempUserKey = (String) session.getAttribute("OAUTH2_TEMP_USER_KEY");
        if (tempUserKey == null) {
            // 공통에러처리예정
            throw new IllegalArgumentException("임시 사용자 키가 세션에 없습니다.");
        }
        */
        TempUserInfo tempUserInfo = redisService.getValues(tempUserKey, TempUserInfo.class);

        if (tempUserInfo == null) {
            // 공통에러처리예정
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("임시 사용자 정보 없음");
        }

        userService.createUser(tempUserInfo, request);

        redisService.deleteValues(tempUserKey);

        // 임시 응답값
        return ResponseEntity.ok("회원가입 완료");
    }

    /**
     * 로그아웃시 refreshToekn 쿠키, redis 삭제
     *
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @return 로그아웃 완료 응답
     */
    @PostMapping("/logout")
    public ResponseEntity<?> userLogout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);
        SecurityContextHolder.clearContext();
        // 임시 응답값
        return ResponseEntity.ok("로그아웃 완료");
    }

}
