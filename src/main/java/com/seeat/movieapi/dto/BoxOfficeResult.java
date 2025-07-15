package com.seeat.movieapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class BoxOfficeResult {
    private String boxofficeType;
    private String showRange;
    private List<DailyBoxOffice> dailyBoxOfficeList;
}
