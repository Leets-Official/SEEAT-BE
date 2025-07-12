package com.seeat.server.domain.theater.domain.entity;

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
public class Theater {

    @Id
    private String code;

    private String name;

    private String address;

    private Double latitude;

    private Double longitude;
}
