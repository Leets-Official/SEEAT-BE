package com.seeat.movieapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class MovieListDto {

    private String movieCd;
    private String movieNm;
    private String movieNmEn;
    private String prdtYear;
    private String openDt;
    private String typeNm;
    private String prdtStatNm;
    private String nationAlt;
    private String genreAlt;
    private String repNationNm;
    private String repGenreNm;

    private List<Director> directors;
    private List<Company> companys;

    @Data
    public static class Director {
        private String peopleNm;
    }

    @Data
    public static class Company {
        private String companyCd;
        private String companyNm;
    }
}
