package com.example.wayfinderai.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InitialAnalysisResponseDto {

    @JsonProperty("counseling_response")
    private String counselingResponse;

    @JsonProperty("main_emotion")
    private String mainEmotion;

    @JsonProperty("temp_guest_id")
    private String tempGuestId;
}