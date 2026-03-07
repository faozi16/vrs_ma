package com.af.vrs.reservation.application;

import java.util.List;

import com.af.vrs.entity.Feedback;

public interface FeedbackApplicationService {
    Feedback saveFeedback(Feedback feedback);

    List<Feedback> getAllFeedbacks();

    Feedback getFeedbackById(Long feedbackId);

    Feedback updateFeedback(Long feedbackId, Feedback feedbackDetails);

    void deleteFeedback(Long feedbackId);
}
