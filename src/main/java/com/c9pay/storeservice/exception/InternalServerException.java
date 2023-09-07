package com.c9pay.storeservice.exception;

public class InternalServerException extends RuntimeException {

    public InternalServerException(){
        super("외부 서비스와의 통신을 실패하였습니다.");
    }
}
