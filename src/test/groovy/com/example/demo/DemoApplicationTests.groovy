package com.example.demo


import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.batch.core.Step
import spock.lang.Specification

import javax.sql.DataSource

@SpringBatchTest
@SpringBootTest(classes = [JobConfig, BatchConfiguration, DemoApplication])
class DemoApplicationTests extends Specification {

	@Autowired
	JobLauncherTestUtils jobLauncherTestUtils

	@Autowired
	Job myJob

	@Autowired
	JobLauncher jobLauncher

	@Autowired
	DataSource dataSource

	@Autowired
	@Qualifier("workingPrimaryStep")
	Step workingPrimaryStep

	@Autowired
	@Qualifier("brokenSecondaryStep")
	Step brokenSecondaryStep

	@Autowired
	@Qualifier("workingListener")
	StepInterfaceListener workingListener

	@Autowired
	@Qualifier("brokenListener")
	StepInterfaceListener brokenListener

	def setup() {
		jobLauncherTestUtils.setJob(myJob)
	}

	void 'print db url'() throws Exception {
		when:
		System.out.println(dataSource.getConnection().getMetaData().getURL());

		then:
		noExceptionThrown()
	}

	def "test listeners"() {
		when:
		JobExecution result = jobLauncher.run(myJob, new JobParameters())

		then:
		workingListener.beforeStepCalled
		brokenListener.beforeStepCalled
	}
}
