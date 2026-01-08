package com.example.demo


import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener

class MyJobListener implements JobExecutionListener {
    @Override
    void beforeJob(JobExecution jobExecution) {
        System.out.println("Before Job")
    }

    /**
     * The job can fail at various steps throughout, this ensures that we always send the notification
     * regardless of where it fails.
     *
     * @param jobExecution
     */
    @Override
    void afterJob(JobExecution jobExecution) {
        System.out.println("After Job")
    }
}