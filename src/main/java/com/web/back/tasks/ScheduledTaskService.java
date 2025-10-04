package com.web.back.tasks;

import com.web.back.services.EvaluationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class ScheduledTaskService {
    private final EvaluationService evaluationService;

    public ScheduledTaskService(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    // Runs every day at 04:00 AM
    @Scheduled(cron = "0 0 4 * * ?")
    public void runDailyTask() {
        Instant beginDate = Instant.now().minus(Duration.ofDays(15));
        Instant endDate = Instant.now();
        evaluationService.generateEvaluations(beginDate, endDate);
    }
}
