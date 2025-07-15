package com.seeat.movieapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class MovieInfo {
    private String movieCd;
    private String movieNm;
    private String movieNmEn;
    private String movieNmOg;
    private String prdtYear;
    private String showTm;
    private String openDt;
    private String prdtStatNm;
    private String typeNm;

    private List<Nation> nations;
    private List<Genre> genres;
    private List<Director> directors;
    private List<Actor> actors;
    private List<ShowType> showTypes;
    private List<Audit> audits;
    private List<Company> companys;
    private List<Staff> staffs;
}

