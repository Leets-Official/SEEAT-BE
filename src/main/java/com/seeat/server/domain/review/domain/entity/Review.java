package com.seeat.server.domain.review.domain.entity;

import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영화 리뷰 엔티티
 * - 리뷰 작성자(User), 좌석(Seat)과 연관
 * - 영화 제목, 평점, 내용, 이미지 URL 저장
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

    @ManyToOne(fetch = FetchType.LAZY)
    private Seat seat;

    /// 같이 입력된 리뷰를 확인하기 위한 식별자
    private String groupId;

    private String title;

    private String movieTitle;

    private String thumbnailUrl;

    private double rating;

    private String content;

    /// 정적 팩토리 메서드
    public static Review of(User user, Seat seat, String movieTitle, double rating, String content, String title, String groupId) {

        return Review.builder()
                .user(user)
                .seat(seat)
                .title(title)
                .movieTitle(movieTitle)
                .thumbnailUrl("thumbnailUrl")
                .rating(rating)
                .content(content)
                .groupId(groupId)
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
