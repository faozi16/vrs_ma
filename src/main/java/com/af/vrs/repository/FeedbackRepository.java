package com.af.vrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.af.vrs.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
