package com.seeat.movieapi.dto;

public class Cinema {
    private String name;  // 추가
    private String cinemaId;
    private String screenId;
    private String playDate;
    private String playSequence;
    private String screenDivisionCode;

    public String getName() {
        return name;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public String getScreenId() {
        return screenId;
    }

    public String getPlayDate() {
        return playDate;
    }

    public String getPlaySequence() {
        return playSequence;
    }

    public String getScreenDivisionCode() {
        return screenDivisionCode;
    }
}
