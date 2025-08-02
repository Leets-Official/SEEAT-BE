package com.seeat.server.domain.best.domain.entity;

import com.seeat.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BestReviewSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long reviewId;

    @ElementCollection
    @CollectionTable(name = "best_review_snapshot_hashtags", joinColumns = @JoinColumn(name = "best_review_snapshot_id"))
    @Column(name = "hashtag")
    private List<String> hashTags = new ArrayList<>();

    private String thumbnailUrl;

    private String movieTitle;

    private String theaterName;

    private String title;

    private String content;

    private Long userId;

    private String nickname;

    private String profileImageUrl;

    private Long heartCount;

    private LocalDateTime originalCreatedAt;

    /// 정적 팩토리 메서드
    public static BestReviewSnapshot of(
            Long reviewId,
            List<String> hashTags,
            String thumbnailUrl,
            String movieTitle,
            String theaterName,
            String title,
            String content,
            Long userId,
            String nickname,
            String profileImageUrl,
            Long heartCount,
            LocalDateTime originalCreatedAt
    ) {
        return BestReviewSnapshot.builder()
                .reviewId(reviewId)
                .hashTags(hashTags)
                .thumbnailUrl(thumbnailUrl)
                .movieTitle(movieTitle)
                .theaterName(theaterName)
                .title(title)
                .content(content)
                .userId(userId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .heartCount(heartCount)
                .originalCreatedAt(originalCreatedAt)
                .build();
    }


}

