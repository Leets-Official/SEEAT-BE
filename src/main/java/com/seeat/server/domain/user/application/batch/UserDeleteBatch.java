package com.seeat.server.domain.user.application.batch;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDeleteBatch {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 0시 실행
    public void deleteUsersMonthly() {

        // updated 30일 지난것만 지우기
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<User> usersToDelete = userRepository.findAllByIsDeleteTrueAndUpdatedAtBefore(cutoffDate);

        usersToDelete.forEach(userRepository::delete);
    }
}
