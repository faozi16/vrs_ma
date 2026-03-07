package com.af.vrs.mapper;

import org.mapstruct.Mapper;

import com.af.vrs.dto.FeedbackDto;
import com.af.vrs.entity.Feedback;

@Mapper(
    componentModel = "spring"
)
public interface FeedbackMapper {
    FeedbackDto feedbackToFeedbackDto(Feedback feedback);
    Feedback feedbackDtoToFeedback(FeedbackDto feedbackDto);
}
