package com.infybuzz.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/job")
public class JobController {
    @Autowired
    JobLauncher jobLauncher;

    @Autowired
   // @Qualifier("second-job")
    Job chunkjob;
    @Autowired
    JobOperator jobOperator;

    @GetMapping("/launch/{jobName}")
    public String launchJob(@PathVariable(name = "jobName") String jobName,
                            @RequestBody String jobName2){
        System.out.println(jobName2);
        Map<String, JobParameter> jobParametersMap = new HashMap<>();
        jobParametersMap.put("currentTime",new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(jobParametersMap);
        long executionId = 0;
        try {
           JobExecution jobExecution =  jobLauncher.run(chunkjob,jobParameters);
           executionId = jobExecution.getId();
        } catch (JobExecutionAlreadyRunningException e) {
            throw new RuntimeException(e);
        } catch (JobRestartException e) {
            throw new RuntimeException(e);
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new RuntimeException(e);
        } catch (JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
        return "Job Started !!" + executionId;
    }

    @GetMapping("/stop/{executionId}")
    public String stopJob(@PathVariable("executionId") Long executionId){
        System.out.println("stopping job with execution id " + executionId);
        try {
            jobOperator.stop(executionId);
        } catch (NoSuchJobExecutionException e) {
            throw new RuntimeException(e);
        } catch (JobExecutionNotRunningException e) {
            throw new RuntimeException(e);
        }
        return "Job Stopped";
        //in chunk processing when you stop running job spring batch will complete ongoing chunk batch
        //and then stop next  chunks batch
    }
}
