package es.wrapitup.wrapitup_planner.dto;

import java.util.List;
import lombok.Data;

@Data
public class QuizResultDTO {
    private Integer quizScore;
    private Integer quizMaxScore;
    private List<Double> quizProgressPercentages;
}