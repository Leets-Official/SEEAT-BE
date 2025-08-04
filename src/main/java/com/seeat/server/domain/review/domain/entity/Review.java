package com.seeat.server.domain.review.domain.entity;

import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영화 리뷰 엔티티
 * - 리뷰 작성자(User)
 * - 영화 제목, 평점, 내용, 이미지 URL 저장
 * - 좌석은 중간테이블로 설정하여 처리한다.
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String title;

    private String movieTitle;

    private String thumbnailUrl;

    private double rating;

    private String content;

    /// 정적 팩토리 메서드
    public static Review of(User user, String movieTitle, double rating, String content, String title) {

        return Review.builder()
                .user(user)
                .title(title)
                .movieTitle(movieTitle)
                .thumbnailUrl("thumbnailUrl")
                .rating(rating)
                .content(content)
                .build();
    }

    /// 썸네일 추가 함수
    public void changeThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /// 수정하는 함수
    public void updateReview(double rating, String content, String title) {
        this.title = title;
        this.rating = rating;
        this.content = content;
    }

}
