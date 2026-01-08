package com.example.demo

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.PartitionStepBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler
import org.springframework.batch.item.support.ListItemReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration(proxyBeanMethods = false)
class JobConfig {

    @Autowired
    JobRepository jobRepository

    @Bean
    SyncTaskExecutor myExecutor() {
        new SyncTaskExecutor()
    }

    @Bean
    MyJobListener myJobListener() {
        new MyJobListener()
    }

    @Bean
    ThreadPoolTaskExecutorBuilder slaveThreadPoolTaskExecutorBuilder() {
        new ThreadPoolTaskExecutorBuilder()
                .corePoolSize(50)
                .maxPoolSize(50)
                //.taskDecorator(systemReportAccountingModeTaskDecorator)
    }

    @Bean
    @JobScope
    TaskExecutor myTaskExecutor(ThreadPoolTaskExecutorBuilder slaveThreadPoolTaskExecutorBuilder) {
        slaveThreadPoolTaskExecutorBuilder.threadNamePrefix('myTask-').build()
    }

    @Bean
    Job myJob(JobRepository jobRepository, @Qualifier('workingPrimaryStep') Step workingPrimaryStep, @Qualifier('brokenPrimaryStep') Step brokenPrimaryStep, @Qualifier('myJobListener') MyJobListener myJobListener) {
        new JobBuilder("myJob", jobRepository)
                .start(workingPrimaryStep)
                .next(brokenPrimaryStep)
                .listener(myJobListener)
                .build()
    }

    @Bean
    Step workingPrimaryStep( @Qualifier('workingSecondaryStep') Step workingSecondaryStep, @Qualifier('myTaskExecutor') TaskExecutor myTaskExecutor) {
        PartitionStepBuilder stepBuilder = new StepBuilder('workingPrimaryStep', jobRepository)
                .allowStartIfComplete(true)
                .partitioner("rentalBillingExecutionSecondaryStep", new FixedRangePartitioner())
                .step(workingSecondaryStep)
                .gridSize(100)
        stepBuilder.taskExecutor(myTaskExecutor)

        stepBuilder.build()
    }

    @Bean
    Step workingSecondaryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, @Qualifier('workingListener') StepInterfaceListener workingListener, @Qualifier('myExecutor') SyncTaskExecutor myExecutor) {
        // Listener BEFORE chunk: Interface method IS called
        new StepBuilder('workingSecondaryStep', jobRepository)
                .allowStartIfComplete(true)
                .listener(workingListener)
                .chunk(1, transactionManager)
                .reader(new ListItemReader<>([1]))
                .writer({ items -> })
                .faultTolerant()
                .skip(Exception.class)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .taskExecutor(myExecutor)
                .build()
    }

    @Bean
    Step brokenPrimaryStep( @Qualifier('brokenSecondaryStep') Step brokenSecondaryStep, @Qualifier('myTaskExecutor') TaskExecutor myTaskExecutor) {
        PartitionStepBuilder stepBuilder = new StepBuilder('brokenPrimaryStep', jobRepository)
                .allowStartIfComplete(true)
                .partitioner("rentalBillingExecutionSecondaryStep", new FixedRangePartitioner())
                .step(brokenSecondaryStep)
                .gridSize(100)
        stepBuilder.taskExecutor(myTaskExecutor)

        stepBuilder.build()
    }

    @Bean
    Step brokenSecondaryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, @Qualifier('brokenListener') StepInterfaceListener brokenListener, @Qualifier('myExecutor') SyncTaskExecutor myExecutor) {
        // Listener AFTER chunk: Interface method might NOT be called in certain SB5 versions
        new StepBuilder('brokenSecondaryStep', jobRepository)
                .allowStartIfComplete(true)
                .chunk(1, transactionManager)
                .listener(brokenListener)
                .reader(new ListItemReader<>([1]))
                .writer({ items -> })
                .faultTolerant()
                .skip(Exception.class)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .taskExecutor(myExecutor)
                .build()
    }

    @Bean
    StepInterfaceListener workingListener() { new StepInterfaceListener() }

    @Bean
    StepInterfaceListener brokenListener() { new StepInterfaceListener() }
}