package com.seeat.server.domain.theater.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MovieGenre {
    ACTION("액션"),
    HORROR("호러"),
    THRILLER("스릴러"),
    COMEDY("코미디"),
    SF("SF"),
    ROMANCE("로맨스"),
    FANTASY("판타지"),
    MYSTERY("미스터리"),
    CRIME("범죄"),
    ADVENTURE("모험"),
    WAR("전쟁"),
    HISTORY("역사"),
    MUSICAL("뮤지컬"),
    ANIMATION("애니메이션"),
    DRAMA("드라마"),
    DOCUMENT("다큐");

    private final String displayName;

    @JsonIgnore
    private String getGenre() {
        return displayName;
    }
}
