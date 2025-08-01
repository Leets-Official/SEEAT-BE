package com.seeat.server.domain.user.application.batch;

import com.seeat.server.domain.user.application.usecase.UserProfileUseCase;
import com.seeat.server.domain.user.domain.UserFixtures;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserDeleteBatchTest {

    @Autowired
    private UserProfileUseCase userProfileService;

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserDeleteBatch sut;

    @Test
    @DisplayName("탈퇴 30일 지난 유저 정상 삭제")
    void deleteUser_Success() {
        // given
        User user = repository.save(UserFixtures.createUser());
        userProfileService.deactivateUser(user.getId());

        repository.updateUpdatedAtById(user.getId(), LocalDateTime.now().minusDays(31));

        // when
        sut.deleteUsersMonthly();

        // then
        assertThat(repository.findById(user.getId())).isEmpty();
    }

}
