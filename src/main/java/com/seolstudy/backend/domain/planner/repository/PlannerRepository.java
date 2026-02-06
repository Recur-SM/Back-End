package com.seolstudy.backend.domain.planner.repository;

import com.seolstudy.backend.domain.planner.entity.Planner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PlannerRepository extends JpaRepository<Planner, Long> {

    Optional<Planner> findByMenteeIdAndPlannerDate(Long menteeId, LocalDate plannerDate);

    boolean existsByMenteeIdAndPlannerDate(Long menteeId, LocalDate plannerDate);
}
