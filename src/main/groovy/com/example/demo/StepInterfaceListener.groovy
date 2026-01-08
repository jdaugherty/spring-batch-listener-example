package com.example.demo

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener

class StepInterfaceListener implements StepExecutionListener {
    boolean beforeStepCalled = false

    @Override
    void beforeStep(StepExecution stepExecution) {
        beforeStepCalled = true
    }

    @Override
    ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus()
    }
}
