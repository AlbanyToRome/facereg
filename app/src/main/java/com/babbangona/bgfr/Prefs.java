package com.babbangona.bgfr;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public  static final  String FACE_MATCH     = "FACE_MATCH_BOOLEAN";
    public static final int FACE_MATCH_DEFAULT  = 0;

    public Prefs(Context mCtx){
        preferences                             = PreferenceManager.getDefaultSharedPreferences(mCtx);
        editor                                  = preferences.edit();
    }

    public void write(String key, String value){
        editor.putString(key,value);
        editor.apply();
    }

    public void write(String key, Long value){
        editor.putLong(key,value);
        editor.apply();
    }

    public void write(String key, int value){
        editor.putInt(key,value);
        editor.apply();
    }

    public String getValue(String key, String value){
        return preferences.getString(key,value);
    }

    public int getValue(String key, int value){
        return preferences.getInt(key,value);
    }

    public int getFaceMatch(){
        return getValue(FACE_MATCH,FACE_MATCH_DEFAULT);
    }
    public  void putFaceMatch(int match){
        write(FACE_MATCH,match);
    }
}
