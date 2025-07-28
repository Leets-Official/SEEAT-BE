package com.seeat.server.global.response.pageable;

import org.springframework.data.domain.Sort;

public class PageUtil {

    public static org.springframework.data.domain.PageRequest getPageable(PageRequest pageRequest) {
        return org.springframework.data.domain.PageRequest.of
                (pageRequest.getPage() - 1, pageRequest.getSize());
    }

    public static org.springframework.data.domain.PageRequest getPageable(PageRequest pageRequest, Sort sort) {

        return org.springframework.data.domain.PageRequest.of
                (pageRequest.getPage() - 1, pageRequest.getSize(), sort);
    }

}
