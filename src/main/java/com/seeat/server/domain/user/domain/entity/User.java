package com.seeat.server.domain.user.domain.entity;

import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.entity.Seat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티
 * - 기본 회원 정보 (아이디, 이메일, 닉네임, 프로필 이미지)
 * - 선호 장르 목록은 Enum 리스트로 관리 (@ElementCollection 사용)
 * - 소셜 로그인 유형, 권한, 등급은 Enum으로 관리
 * - 다른 소셜 로그인이여도 이메일이 중복되면 가입불가이기에 socialId로 구분
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "`user`")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String socialId;

    private String username;

    private String nickname;

    private String imageUrl;

    @Builder.Default
    @Column(name = "genre")
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_genres", joinColumns = @JoinColumn(name = "user_id"))
    private List<MovieGenre> genres = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private UserSocial social;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserGrade grade;

    // 사용자 생성 정적 팩토리 메서드
    public static User of(String email, String socialId, UserSocial social, String username, String nickname, String imageUrl, List<MovieGenre> genres) {
        return User.builder()
                .email(email)
                .socialId(socialId)
                .social(social)
                .username(username)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .genres(genres)
                .role(UserRole.USER)
                .grade(UserGrade.BRONZE)
                .build();
    }

    // 사용자 정보 수정 메소드
    public void updateUser(String nickname, String imageUrl, List<MovieGenre> genres) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.genres = genres;
    }
}
