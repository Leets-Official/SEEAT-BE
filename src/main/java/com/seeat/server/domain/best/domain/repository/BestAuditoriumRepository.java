package com.seeat.server.domain.best.domain.repository;

import com.seeat.server.domain.best.domain.entity.BestAuditoriumSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BestAuditoriumRepository extends JpaRepository<BestAuditoriumSnapshot, Long> {

}
