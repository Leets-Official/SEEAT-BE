package com.seeat.server.domain.theater.presentation;

import com.seeat.server.domain.theater.presentation.swagger.TheaterControllerSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/theaters")
public class TheaterController implements TheaterControllerSpec {
}
