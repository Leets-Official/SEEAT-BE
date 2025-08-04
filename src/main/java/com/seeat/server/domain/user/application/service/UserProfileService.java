package com.seeat.server.domain.user.application.service;

import com.seeat.server.domain.image.application.usecase.ImageUseCase;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.review.domain.repository.dto.ReviewLikeCount;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.user.application.dto.request.UserInfoUpdateRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.application.usecase.UserProfileUseCase;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserAuditorium;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.repository.UserAuditoriumRepository;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileService implements UserProfileUseCase {

    private final UserUseCase service;

    // 외부 의존성
    private final UserAuditoriumRepository userAuditoriumRepository;
    private final ReviewRepository reviewRepository;
    private final TheaterUseCase theaterService;

    /// 이미지 서비스 추가
    private final ImageUseCase imageService;

    /**
     * 마이페이지 사용자 정보 조회를 위한 로직
     *
     * @param userId 정보를 조회할 사용자 Id
     * @return 사용자 정보에 대한 조회 DTO
     */
    @Override
    public UserInfoResponse getUserInfo(Long userId){

        // 사용자 예외처리 및 사용자 정보 조회
        User user = service.getUser(userId);

        // 상영관 n+1 방지 fetch join, 예외 처리
        List<Auditorium> auditorium = getAuditoriums(userId);

        Long reviewCount = 0L;
        Long likeCount = 0L;

        // 리뷰, 좋아요 수 가져오기
        Optional<ReviewLikeCount> count = reviewRepository.findReviewAndLikeCountByUserId(userId);
        if (count.isPresent()){
            reviewCount = count.get().getReviewCount();
            likeCount = count.get().getLikeCount();
        }

        // 경험치 계산
        double levelExp = calculateLevelExp(user.getGrade(), reviewCount, likeCount);

        // 리뷰 개수와 좋아요 개수에 따라 등급 수정
        UserGrade grade = calculateUserGrade(reviewCount, likeCount);

        // 다르면 등급 수정
        if (user.getGrade() != grade){
            user.updateGrade(grade);
        }

        return UserInfoResponse.from(user, auditorium, reviewCount, likeCount, levelExp);
    }

    /**
     * 마이페이지 사용자 정보 수정을 위한 로직
     * @param userId 정보를 수정할 사용자 Id
     * @param request 수정할 정보 DTO
     * @return 수정된 정보 DTO
     */
    @Override
    public UserInfoUpdateResponse updateUserInfo(Long userId, UserInfoUpdateRequest request) throws IOException {

        // 사용자 예외 처리
        User user = service.getUser(userId);

        // request 상영관 예외 처리
        List<Auditorium> auditoriums = request.getAuditoriumIds().stream()
                .map(theaterService::getAuditorium)
                .toList();

        /// 기존 이미지 사진이 기본 값
        String thumbnailImage = user.getImageUrl();

        /// 존재한다면 이미지 추가
        if (request.getImage() != null) {

            /// 기존 사진 사진
            imageService.deleteFile(thumbnailImage);

            thumbnailImage = request.getImage();
        }

        // 사용자 정보 수정
        user.updateUser(request.getNickname(), thumbnailImage, request.getGenres());

        // 기존 userAuditorium 삭제
        userAuditoriumRepository.deleteByUserId(userId);

        // 새 상영관으로 저장
        List<UserAuditorium> userAuditoriums = auditoriums.stream()
                .map(auditorium -> UserAuditorium.of(user, auditorium))
                .collect(Collectors.toList());

        userAuditoriumRepository.saveAll(userAuditoriums);

        // 사용자 업데이트 DTO로 변환
        return UserInfoUpdateResponse.from(user, auditoriums);
    }

    /**
     * 사용자 등급 목록 조회를 위한 로직
     *
     * @return UserGradeResponse DTO List 응답
     */
    @Override
    public List<UserGradeResponse> getUserGradeList(){

        // 유저 등급 목록 조회
        return Arrays.stream(UserGrade.values())
                .map(UserGradeResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 탈퇴 상태로 수정하는 로직
     *
     * @param userId 유저 Id
     */
    @Override
    public void deactivateUser(Long userId){
        // 사용자 예외처리 및 사용자 정보 조회
        User user = service.getUser(userId);

        // 더티체킹으로 변경
        user.deactivateUser();
    }

    /**
     * 등급 계산 로직
     */
    private UserGrade calculateUserGrade(long reviewCount, long likeCount) {
        if (reviewCount >= 40 && likeCount >= 100) {
            return UserGrade.PLATINUM;
        } else if (reviewCount >= 10 && likeCount >= 25) {
            return UserGrade.GOLD;
        } else if (reviewCount >= 2 && likeCount >= 5) {
            return UserGrade.SILVER;
        } else {
            return UserGrade.BRONZE;
        }
    }

    /**
     * 경험치 계산 로직
     * @param grade 유저 등급
     * @param reviewCount 리뷰 개수
     * @param likeCount 좋아요 개수
     * @return 경험치
     */
    private double calculateLevelExp(UserGrade grade, long reviewCount, long likeCount) {
        double reviewRate;
        double likeRate;

        // 등급 별 경험치 계산법
        switch (grade) {
            case BRONZE -> {
                reviewRate = Math.min((double) reviewCount / 2, 1.0);
                likeRate = Math.min((double) likeCount / 5, 1.0);
            }
            case SILVER -> {
                reviewRate = Math.min((double) reviewCount / 10, 1.0);
                likeRate = Math.min((double) likeCount / 25, 1.0);
            }
            case GOLD -> {
                reviewRate = Math.min((double) reviewCount / 40, 1.0);
                likeRate = Math.min((double) likeCount / 100, 1.0);
            }
            case PLATINUM -> {
                return 100.0;
            }
            default -> throw new IllegalArgumentException(ErrorCode.NOT_GRADE.getMessage());
        }

        return (reviewRate + likeRate) / 2 * 100;
    }


    /// 공통 함수
    private List<Auditorium> getAuditoriums(Long userId) {
        List<Auditorium> auditoriums = userAuditoriumRepository.findAuditoriumsByUserId(userId);

        if (auditoriums.isEmpty()) {
            throw new NoSuchElementException(ErrorCode.NOT_AUDITORIUM.getMessage());
        }

        return auditoriums;
    }

}
