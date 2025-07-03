package com.seeat.server.domain.user.presentation;


import com.seeat.server.domain.user.application.UserService;
import com.seeat.server.domain.user.application.dto.UserSignUpRequest;
import com.seeat.server.domain.user.presentation.swagger.UserControllerSpec;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.security.jwt.service.TokenService;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerSpec {

    private final RedisService redisService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> userSignUp(@RequestBody UserSignUpRequest request, //HttpSession session
                                        @RequestHeader("Temp-User-Key") String tempUserKey){
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


}
