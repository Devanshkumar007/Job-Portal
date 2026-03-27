package com.capg.JobService.exceptions;

public class JobNotFoundException extends RuntimeException{
    public JobNotFoundException(String msg) {
        super(msg);
    }
}
