package com.web.back.controllers;

import com.web.back.tasks.EvaluationMotorTask;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/task")
@Tag(name = "Task Manager")
public class TaskController {
    private final EvaluationMotorTask evaluationMotorTask;

    public TaskController(EvaluationMotorTask evaluationMotorTask) {
        this.evaluationMotorTask = evaluationMotorTask;
    }

    @RequestMapping("/is-running")
    public ResponseEntity<Boolean> isTaskRunning() {
        return ResponseEntity.ok(evaluationMotorTask.isRunning());
    }

    @PostMapping("/trigger")
    public ResponseEntity<Void> triggerTask(LocalDate beginDate, LocalDate endDate,
                                            String grouper1, String grouper2, String grouper3, String grouper4, String grouper5) {
        new Thread(() ->
                evaluationMotorTask.executeProcess(beginDate, endDate, grouper1, grouper2, grouper3, grouper4, grouper5)
        ).start();

        return ResponseEntity.accepted().build();
    }
}
