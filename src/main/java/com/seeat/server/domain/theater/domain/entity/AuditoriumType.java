package com.seeat.server.domain.theater.domain.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditoriumType {

    DOLBY("DOLBY"),
    IMAX("IMAX"),
    FOURDX("4DX");

    private final String code;

    @JsonValue
    public String getCode() {
        return code;
    }
}
