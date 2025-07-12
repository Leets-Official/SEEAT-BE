package com.seeat.server.domain.review.domain.entity;


import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 유저가 찜(북마크)한 후기 정보를 나타내는 엔티티
 * - 한 유저가 여러 후기를 북마크 할 수 있음
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Review review;

    /// 정적 팩토리 메서드
    public static Bookmark of(User user, Review review) {
        return Bookmark.builder()
                .user(user)
                .review(review)
                .build();
    }

}
