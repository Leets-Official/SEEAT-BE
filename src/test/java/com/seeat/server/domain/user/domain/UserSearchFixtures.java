package com.seeat.server.domain.user.domain;

import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSearch;

public class UserSearchFixtures {

    public static UserSearch createUserSearch(User user, Search search) {
        return UserSearch.builder()
                .user(user)
                .search(search)
                .build();
    }
}
