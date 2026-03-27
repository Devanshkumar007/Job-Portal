package com.capg.authservice.exception;

public class RestrictedUser extends RuntimeException{
    public RestrictedUser(String msg){
        super(msg);
    }
}
