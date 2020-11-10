package com.babbangona.bgfr;

public enum BGFRMode {
    AUTHENTICATE,
    CAPTURE_NEW;
    public BGFRMode set(int id){
        if(id > 1 || id <0 )
        {
            return BGFRMode.values()[0];
        }
        else
            return BGFRMode.values()[id];
    }
}