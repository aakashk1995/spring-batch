package com.infybuzz.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FirstJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        //executed before starting job
        System.out.println("Executing before Job ");
        System.out.println(jobExecution.getJobInstance().getInstanceId());
        System.out.println(jobExecution.getJobInstance().getJobName());
        jobExecution.getExecutionContext().put("one","1");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        //executed after job finishes execution
        System.out.println("Executing after job");
        System.out.println(jobExecution.getExecutionContext().get("one"));
    }
}
