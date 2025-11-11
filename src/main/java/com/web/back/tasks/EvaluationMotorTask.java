package com.web.back.tasks;

import com.web.back.process.EvaluationAtLevelOneProcess;
import com.web.back.process.EvaluationAtLevelTwoProcess;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class EvaluationMotorTask {
    @Getter
    private volatile boolean running = false;
    private static final Logger logger = LoggerFactory.getLogger(EvaluationMotorTask.class);

    private final EvaluationAtLevelOneProcess evaluationAtLevelOneProcess;
    private final EvaluationAtLevelTwoProcess evaluationAtLevelTwoProcess;

    public EvaluationMotorTask(EvaluationAtLevelOneProcess evaluationAtLevelOneProcess, EvaluationAtLevelTwoProcess evaluationAtLevelTwoProcess) {
        this.evaluationAtLevelOneProcess = evaluationAtLevelOneProcess;
        this.evaluationAtLevelTwoProcess = evaluationAtLevelTwoProcess;
    }

    public void executeProcess(LocalDate beginDate, LocalDate endDate,
                               String grouper1, String grouper2, String grouper3, String grouper4, String grouper5) {
        running = true;
        String runId = UUID.randomUUID().toString();
        logger.info("Manual task started. Run ID: {}", runId);
        try {
            evaluationAtLevelOneProcess.generateEvaluations(beginDate, endDate, grouper1, grouper2, grouper3, grouper4, grouper5);
            evaluationAtLevelTwoProcess.executeLevelTwoRules(beginDate, endDate);
        } finally {
            running = false;
            logger.info("Manual task finished. Run ID: {}", runId);
        }
    }

    // Runs every day at 04:00 AM
    @Scheduled(cron = "0 0 4 * * ?")
    public void runDailyTask() {
        running = true;
        String runId = UUID.randomUUID().toString();
        logger.info("Scheduled task started. Run ID: {}", runId);
        try {
            LocalDate beginDate = LocalDate.now().minus(Duration.ofDays(15));
            LocalDate endDate = LocalDate.now();
            evaluationAtLevelOneProcess.generateEvaluations(beginDate, endDate);
            evaluationAtLevelTwoProcess.executeLevelTwoRules(beginDate, endDate);
        } finally {
            running = false;
            logger.info("Scheduled task finished. Run ID: {}", runId);
        }
    }
}
