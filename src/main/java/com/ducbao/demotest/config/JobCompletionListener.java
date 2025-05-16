package com.ducbao.demotest.config;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JobCompletionListener extends JobExecutionListenerSupport {
    @Override
    public void beforeJob(org.springframework.batch.core.JobExecution jobExecution) {
        System.out.println("Job started at: " + jobExecution.getStartTime());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("Job ended at: " + jobExecution.getEndTime());

        Duration duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());

        long seconds = duration.getSeconds();
        long millis = duration.toMillis();

        System.out.println("Job execution time: " + seconds + " seconds (" + millis + " ms)");
    }
}
