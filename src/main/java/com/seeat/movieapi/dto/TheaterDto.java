package com.seeat.movieapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TheaterDto {
    @JsonProperty("SIGUN_NM")
    private String sigunNm;

    @JsonProperty("BIZPLC_NM")
    private String bizplcNm;

    @JsonProperty("LICENSG_DE")
    private String licensgDe;

    @JsonProperty("BSN_STATE_NM")
    private String bsnStateNm;

    @JsonProperty("LOCPLC_FACLT_TELNO_DTLS")
    private String tel;

    @JsonProperty("REFINE_ROADNM_ADDR")
    private String roadAddress;

    @JsonProperty("REFINE_LOTNO_ADDR")
    private String lotnoAddress;

    @JsonProperty("REFINE_ZIPNO")
    private String zipCode;

    @JsonProperty("REFINE_WGS84_LAT")
    private String lat;

    @JsonProperty("REFINE_WGS84_LOGT")
    private String lon;
}

