package com.seeat.server.domain.manage.domain.entity;

import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Feedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String content;

    public static Feedback of(User user, String content) {
        return Feedback.builder()
            .user(user)
            .content(content)
            .build();
    }

}
