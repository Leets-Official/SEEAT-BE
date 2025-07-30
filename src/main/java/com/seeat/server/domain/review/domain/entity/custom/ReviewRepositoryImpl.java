package com.seeat.server.domain.review.domain.entity.custom;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.global.response.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom{

    @PersistenceContext
    private final EntityManager em;

    /**
     * 리뷰 검색시 정렬 및 필터 조건에 따라 리뷰를 조회
     *
     * @param condition 검색 조건 DTO
     * @param pageable 페이징
     * @return Slice<Review>
     */
    @Override
    public Slice<Review> searchReviewsWithFilters(ReviewSearchCondition condition, Pageable pageable) {

        // 기본 Select review, seat, reviewLike
        StringBuilder jpql = new StringBuilder(
                "SELECT r FROM Review r " +
                        "JOIN r.seat s " +
                        "LEFT JOIN ReviewLike rl ON rl.review = r "
        );

        // 해시태그 필터가 있을 경우 - 해시태그 조인 추가
        boolean filterHashTags = condition.getHashTagIds() != null && !condition.getHashTagIds().isEmpty();
        if (filterHashTags) {
            jpql.append("JOIN ReviewHashTag rht ON rht.review = r ");
        }

        // 공통 WHERE 절 (영화 제목, 내용 검색)
        // => 사용자 제목 추가
        jpql.append("WHERE (r.movieTitle LIKE :keyword OR r.content LIKE :keyword) ");

        // 상영관 필터
        if (condition.getAuditoriumId() != null) {
            jpql.append("AND s.auditorium.id = :auditoriumId ");
        }

        // 해시태그 필터
        if (filterHashTags) {
            jpql.append("AND rht.hashTag.id IN :hashTagIds ");
        }

        // 좋아요 수 기준 정렬을 위해 그룹화
        jpql.append("GROUP BY r ");

        // 검색한 모든 해시태그가 포함된 리뷰만 필터링
        if (filterHashTags) {
            jpql.append("HAVING COUNT(DISTINCT rht.hashTag.id) = :hashTagCount ");
        }

        // 정렬 조건 분기
        switch (condition.getSort()) {
            // 인기순
            case POPULAR -> jpql.append("ORDER BY COUNT(rl) DESC, r.createdAt DESC");
            // 평점순
            case RATING -> jpql.append("ORDER BY r.rating DESC");
            // 최신순
            case LATEST -> jpql.append("ORDER BY r.createdAt DESC");
            default -> throw new IllegalArgumentException(ErrorCode.INVALID_SORT_TYPE.getMessage());
        }

        // 동적 JPQL 쿼리 생성
        TypedQuery<Review> query = em.createQuery(jpql.toString(), Review.class);
        query.setParameter("keyword", "%" + condition.getKeyword() + "%");


        // 상영관 파라미터 설정
        if (condition.getAuditoriumId() != null) {
            query.setParameter("auditoriumId", condition.getAuditoriumId());
        }

        // 해시태그 파라미터 설정
        if (filterHashTags) {
            query.setParameter("hashTagIds", condition.getHashTagIds());
            query.setParameter("hashTagCount", (long) condition.getHashTagIds().size());
        }

        // 페이징 처리
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize() + 1);

        // 쿼리 실행
        List<Review> results = query.getResultList();
        boolean hasNext = results.size() > pageable.getPageSize();

        // Slice 마지막 요소 제거
        if (hasNext) results.remove(results.size() - 1);

        // Slice 형태로 변환
        return new SliceImpl<>(results, pageable, hasNext);
    }

}
