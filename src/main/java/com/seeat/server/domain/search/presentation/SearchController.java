package com.seeat.server.domain.search.presentation;

import com.seeat.server.domain.search.presentation.swagger.SearchControllerSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController implements SearchControllerSpec {
}
