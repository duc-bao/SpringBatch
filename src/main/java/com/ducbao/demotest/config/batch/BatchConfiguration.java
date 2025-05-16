package com.ducbao.demotest.config.batch;


import com.ducbao.demotest.config.JobCompletionListener;
import com.ducbao.demotest.config.PersonItemProcess;
import com.ducbao.demotest.config.PersonItemReader;
import com.ducbao.demotest.config.PersonItemWrite;
import com.ducbao.demotest.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    private final PersonItemWrite personItemWrite;
    private final PersonItemProcess personItemProcess;
    private final PersonItemReader personItemReader;
    private final JobCompletionListener jobCompletionListener;

    public BatchConfiguration(PersonItemWrite personItemWrite, PersonItemProcess personItemProcess, PersonItemReader personItemReader, JobCompletionListener jobCompletionListener) {
        this.personItemWrite = personItemWrite;
        this.personItemProcess = personItemProcess;
        this.personItemReader = personItemReader;
        this.jobCompletionListener = jobCompletionListener;
    }

    // Tạo bean để điều phối các job,
    @Bean
    public JobOperator jobOperator(JobLauncher jobLauncher,
                                   JobRepository jobRepository,
                                   JobExplorer jobExplorer,
                                   JobRegistry jobRegistry) throws Exception {
        SimpleJobOperator operator = new SimpleJobOperator();
        operator.setJobLauncher(jobLauncher);
        operator.setJobRepository(jobRepository);
        operator.setJobExplorer(jobExplorer);
        operator.setJobRegistry(jobRegistry);
        return operator;
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<Person, Person>chunk(1000, transactionManager)
                .reader(personItemReader)
                .processor(personItemProcess)
                .writer(personItemWrite).build();
    }

    @Bean
    public Job insertJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new JobBuilder("importUserJob", jobRepository)
                .start(step1(jobRepository, platformTransactionManager))
                .listener(jobCompletionListener)
                .build();
    }
}
