/*
package com.babbangona.bgfr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.babbangona.bgfr.Database.BGFRInfo;
import com.babbangona.bgfr.Database.MainRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class TestActivity extends CustomLuxandActivity {

    @Override
    public Boolean setDetectFakeFaces() {
        //SET TO TRUE WHEN WE ARE READY TO DETECT FAKE FACES
        return false;
    }

    @Override
    public Long setTimer() {
        //SET TO WHATEVER COUNT DOWN TIME YOU WANT
        return new Long(15000);
    }

    @Override
    public Boolean setKeepFaces() {
        //SET TO TRUE FOR THE INCREASED LUXAND TRACKER SIZE
        return false;
    }



    @Override
    public void TimerFinished() {
        //IF NO FACE FOUND
        //TODO: WHAT YOU WANT TO DO WHEN NO FACE FOUND AND TIMER TIMES OUT
        if (!getBGFR_FACEFOUND()){
            showErrorAndClose("FACE PREVIOUSLY CAPTURED");
        }

        else
        {
            //NOW CAPTURE
            setBGFR_MODE(BGFRMode.CAPTURE_NEW);
            StartTimer();
        }
    }

    @Override
    public void MyFlow() {

        String Template                             = "0";

        if(!Template.matches("^\\s*0\\s*$")){
            setBGFR_MODE(BGFRMode.AUTHENTICATE);
            LoadTracker(Template, BGFRMode.AUTHENTICATE);
        }
        else
        {
            setBGFR_MODE(BGFRMode.CAPTURE_NEW);
            //SET ID INSTEAD
            loadBlankTracker("ID-" + Math.random());
        }

    }

    @Override
    public void Authenticated() {
        showErrorAndClose("SCAMMER");

    }

    @Override
    public void TrackerSaved() {
        BGFRInfo info =  new BGFRInfo(this);
        save(info.getSingleTemplate(),info.getBundledTemplate());

    }

    public void save(String template, String pile){
        showErrorAndClose("SAVED");




    }

    @Override
    public void customAlert(String s){

    }

    @Override
    public String buttonText(){
        return "CAPTURE";
    }

    @Override
    public String headerText(){
        return "CAPTURE";
    }
}
*/
