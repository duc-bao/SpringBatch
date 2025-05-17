package com.ducbao.demotest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job")
public class JobController {
    private final JobLauncher jobLauncher;
    private final RetryTemplate retryTemplate;
    @Qualifier("")
    private final Job job;

    public JobController(JobLauncher jobLauncher, RetryTemplate retryTemplate, Job job) {
        this.jobLauncher = jobLauncher;
        this.retryTemplate = retryTemplate;
        this.job = job;
    }

    @PostMapping("")
    public String startBatchJob() throws JobExecutionException {
        return retryTemplate.execute(context -> {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, jobParameters);
            return "Batch job has been started";
        });
    }
}
