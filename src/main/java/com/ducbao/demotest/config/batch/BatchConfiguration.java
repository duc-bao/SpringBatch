package com.ducbao.demotest.config.batch;


import com.ducbao.demotest.config.*;
import com.ducbao.demotest.config.dataRandom.DataGenerate;
import com.ducbao.demotest.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

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
    @Bean
    @StepScope
    public PersonItemReader personItemReader(
            DataGenerate dataGenerate,
            @Value("#{stepExecutionContext['fromId']}") Integer fromId,
            @Value("#{stepExecutionContext['toId']}") Integer toId) {

        return new PersonItemReader(dataGenerate, fromId, toId);
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
    public Step workerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           PersonItemReader personItemReader) {
        return new StepBuilder("workerStep", jobRepository)
                .<Person, Person>chunk(500, transactionManager)
                .reader(personItemReader)
                .processor(personItemProcess)
                .writer(personItemWrite)
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           PersonPartition partitioner, Step workerStep) {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner("workerStep", partitioner)
                .step(workerStep)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean("partitionJob")
    public Job partitionJob(JobRepository jobRepository,
                            @Qualifier("masterStep") Step masterStep) {
        return new JobBuilder("partitionUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(masterStep)
                .listener(jobCompletionListener)
                .build();
    }
    @Bean
    TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("spring_batch_thread_");
        executor.initialize();
        return executor;
    }
    @Bean
    public RetryTemplate retryTemplate() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(CannotAcquireLockException.class, true);
        retryableExceptions.put(DeadlockLoserDataAccessException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(5, retryableExceptions);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(5000);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

}
