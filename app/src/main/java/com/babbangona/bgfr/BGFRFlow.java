package com.babbangona.bgfr;

public enum BGFRFlow {
    SINGLE_CAPTURE,
    SINGLE_AUTHENTICATE,
    AUTHENTICATE_CAPTURE,
    STACK_CAPTURE;
    public BGFRFlow set(int id){
        if(id > 2 || id <0 )
        {
            return BGFRFlow.values()[0];
        }
        else
            return BGFRFlow.values()[id];
    }
}
