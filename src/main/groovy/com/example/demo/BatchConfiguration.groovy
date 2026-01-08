package com.example.demo

import groovy.transform.CompileStatic
import org.springframework.batch.core.configuration.BatchConfigurationException
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.ExecutionContextSerializer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.autoconfigure.batch.BatchDataSourceScriptDatabaseInitializer
import org.springframework.boot.autoconfigure.batch.BatchProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.sql.init.DatabaseInitializationMode
import org.springframework.boot.sql.init.DatabaseInitializationSettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.annotation.Isolation

import javax.sql.DataSource

@CompileStatic
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BatchProperties)
class BatchConfiguration extends DefaultBatchConfiguration {

    @Override
    JobLauncher jobLauncher(JobRepository jobRepository) throws BatchConfigurationException {
        TaskExecutorJobLauncher launcher = super.jobLauncher(jobRepository) as TaskExecutorJobLauncher
        launcher.setTaskExecutor(new org.springframework.core.task.SyncTaskExecutor()); // blocks
        launcher
    }

    @Bean
    BatchDataSourceScriptDatabaseInitializer batchDataSourceInitializer(DataSource dataSource, BatchProperties properties) {

        new BatchDataSourceScriptDatabaseInitializer(
                dataSource,
                BatchDataSourceScriptDatabaseInitializer.getSettings(dataSource, properties.jdbc)
        )
    }

    @Bean
    JobRegistryBeanPostProcessor jobRegistryPostProcessor() {
        super.jobRegistryBeanPostProcessor()
    }

    @Override
    protected TaskExecutor getTaskExecutor() {
        return new SimpleAsyncTaskExecutor()
    }

    @Override
    protected ExecutionContextSerializer getExecutionContextSerializer() {
        // do not use our own object mapper due to https://github.com/spring-projects/spring-batch/issues/3771
        new Jackson2ExecutionContextStringSerializer()
    }

    @Override
    protected Isolation getIsolationLevelForCreate() {
        Isolation.READ_COMMITTED
    }
}