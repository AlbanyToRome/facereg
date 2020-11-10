package com.babbangona.bgfr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.babbangona.bgfr.Database.LuxandKey;
import com.babbangona.bgfr.Database.MainRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.luxand.FSDK;

import java.util.Calendar;
import java.util.Stack;
import java.util.concurrent.Semaphore;

public abstract class CustomLuxandActivity extends AppCompatActivity implements  BGCallback{

    static Semaphore mutex = new Semaphore(1);

    //===============CLASS WORKING VARIABLES==========================================//
    public static float sDensity                                =  2.0F;
    public static final String TAG                              = "BABBANGONA";
    public boolean mActive                                      = false;
    public static int currentCamera                             = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean mIsFailed                                   = false;
    private boolean wasStopped                                  = false;
    private  int ERROR_CODE                                     = ErrorCodes.KEY_GENERIC_ERROR;
    private FrameLayout mLayout;


    private PowerManager.WakeLock mWakeLock;
    BGPermissions permissions;
    CustomProcess mDraw;
    CustomPreview mPreview;
    //===================================================================================//

    //==============UI ELEMENTS WITH BUTTER KNIFE BINDING================================//
    public TextView countdownTimer;
    public Button captureButton;
    public TextView captureHeader;
    //====================================================================================//
    //===========MASTER TIMER===========================================================//
    private CountDownTimer MASTER_TIMER = null;

    //=======Templates=====================================================================
    Stack<String> Trackers;
    private Handler mHandler;
    public static MainRepository repository;
    //====================================================================================//
    private BGFRMode BGFR_MODE                                  = BGFRMode.CAPTURE_NEW;

    //================BGFR SET UP PARAMETERS============================
    public static BGFRFlow BGFR_FLOW                            = BGFRFlow.SINGLE_CAPTURE;
    private String   BGFR_KEY;
    private String   BGFR_ID;
    private Long     BGFR_TIMER;
    private Boolean  BGFR_FACEFOUND                             = false;
    public Boolean  BGFR_DETECT_FAKE_FACES               = false;
    private Boolean  BGFR_KEEP_FACES                            = false;
    public static String   BGFR_PERSON_TAG                      = "ID" + Math.random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new BGPermissions(this);

        getPrimeSettings();
        setUpUI();



    }

    /**
     * setting up the Views and doing the binding without using the butter knife library
     */
    public void setUpUI(){
        FSDK.Initialize();
        mHandler                                                = new Handler();
        repository                                              = new MainRepository(getApplication());
        mDraw                                                   = new CustomProcess(this,this,BGFR_MODE);
        mDraw.setDetectFakeFaces( setDetectFakeFaces());
        mPreview                                                = new CustomPreview(this, mDraw);
        View localView                                          = LayoutInflater.from(this).inflate(R.layout.capture,null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLayout                                                 = new FrameLayout(this);
        mLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryVariant));
        ViewGroup.LayoutParams params                           = new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        mLayout.setLayoutParams(params);
        setContentView(mLayout);
        mLayout.setVisibility(View.VISIBLE);
        addContentView(mPreview, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addContentView(localView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addContentView(mDraw, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //attach UI Elements
        attachUI();

        // Lock orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Disable capture button
        captureButton.setEnabled(true);
        captureButton.setText(buttonText());
        captureHeader.setText(headerText());
    }

    public void attachUI(){
        captureButton                                           = findViewById(R.id.captureButton);
        countdownTimer                                          = findViewById(R.id.timerText);
        captureHeader                                           = findViewById(R.id.captureID);
    }

    public void loadBlankTracker(String ID){
        BGFR_FACEFOUND                      = false;
        mDraw.feature_name                  = ID;
        mDraw.setMode(BGFRMode.CAPTURE_NEW);
        mDraw.mTracker                      = new FSDK.HTracker();
        if (FSDK.FSDKE_OK != FSDK.CreateTracker(mDraw.mTracker)) {
            showErrorAndClose("ERROR COULD NOT CREATE NEW TRACKER");
        }
        Log.i(TAG, "openCamera: SUCCESS !! FSDK TRACKER CREATED");
        resetTrackerParameters();

    }

    public final void showErrorAndClose(String s) {

        StopTimer();
        customAlert(s);
    }

    public void LoadTracker(String template, BGFRMode mode){
        BGFR_FACEFOUND                      = false;
        mDraw.setMode(mode);
        mDraw.mTracker                      = new FSDK.HTracker();
        if(template != null && doExtraTemplateCheck(template)) {
            byte[] bytes                                        = Base64.decode(template,Base64.DEFAULT);
            int res                                             = FSDK.LoadTrackerMemoryFromBuffer(mDraw.mTracker,bytes);
            if (FSDK.FSDKE_OK != res) {
                showErrorAndClose("CORRUPT OR INVALID TEMPLATE");
            }
            resetTrackerParameters();
        }
        else
            showErrorAndClose("ERROR INVALID OR NULL TEMPLATE");
        resetTrackerParameters();


    }

    public Boolean doExtraTemplateCheck(String template){
        /*if(!template.matches("^RlNES.*") )
        {
            Log.i(TAG, "doExtraTemplateCheck: DID NOT MATCH R1NESwY");
            return  false;
        }*/
        template = template.trim();
        if(template.length() < 800)
        {
            Log.i(TAG, "doExtraTemplateCheck: LENGTH LESS THAN 800");
            return  false;
        }
        else if (!template.startsWith("RlNESwY")){
            return false;
        }
        else if(template.matches(".*003dn$")){
            Log.i(TAG, "doExtraTemplateCheck:HAS 003DN");
            return  false;
        }
        else
        {
            Log.i(TAG, "doExtraTemplateCheck: VALID");
            return true;
        }
    }
    public void resetTrackerParameters(){
        int[] arrayOfInt                                        = new int[1];

        if(!BGFR_KEEP_FACES){
            Log.i(TAG, "resetTrackerParameters: KEEP FACES WAS SET TO FALSE, SMALL SIZED TRACKERS");
            FSDK.SetTrackerMultipleParameters(mDraw.mTracker, getResources().getString(R.string.Luxand_Params_Default), arrayOfInt);
            if (arrayOfInt[0] != 0) {
                showErrorAndClose("Error setting tracker parameters 2, position");
            }
        }
        else if(BGFR_KEEP_FACES){
            Log.i(TAG, "resetTrackerParameters: KEEP FACES WAS SET TO FALSE, SMALL SIZED TRACKERS");
            FSDK.SetTrackerMultipleParameters(mDraw.mTracker, getResources().getString(R.string.Luxand_Params_Keep_Face), arrayOfInt);
            if (arrayOfInt[0] != 0) {
                showErrorAndClose("Error setting tracker parameters 2, position");
            }
        }
        FSDK.SetTrackerMultipleParameters(mDraw.mTracker, "DetectGender=false;DetectExpression=true", arrayOfInt);
        if (arrayOfInt[0] != 0) {
            showErrorAndClose("Error setting tracker parameters 2, position");
        }

        // faster smile detection
        FSDK.SetTrackerMultipleParameters(mDraw.mTracker, "AttributeExpressionSmileSmoothingSpatial=0.5;AttributeExpressionSmileSmoothingTemporal=10;", arrayOfInt);
        if (arrayOfInt[0] != 0) {
            showErrorAndClose("Error setting tracker parameters 3, position");
        }

    }


    public void getPrimeSettings() {


    BGFR_KEEP_FACES                 = setKeepFaces();
    BGFR_TIMER                      = setTimer();
    validateSettings();
    
    }

    public void changeKey(String key){
        int res                                                 = FSDK.ActivateLibrary(key);
        Log.i(TAG, "isKeyValid: BOOLEAN = " + res );
        if (res == 0)
            resetTrackerParameters();
        else
            showErrorAndClose("Invalid Key");


    }


    public boolean isKeyValid(){
        int res                                                 = FSDK.ActivateLibrary(BGFR_KEY);
        Log.i(TAG, "isKeyValid: BOOLEAN = " + res );
        if(res == 0) return true;
        else return  false;
    }


    public void validateSettings(){

        /*if(BGFR_UISTRING.split("-").length < 2)
        {
            BGFR_UISTRING                                       = "Create ID-Capture";
        }*/


        if(!(isKeyValid())){
            BGFR_KEY                                            = new LuxandKey("").setPredefinedKey().Key;
            Log.i(TAG, "validateSettings: " + BGFR_KEY.substring(0,BGFR_KEY.length()/10));
            if(!isKeyValid())showErrorAndClose("INVALID LUXAND ACTIV. KEY");

        }
        if(BGFR_TIMER < 5000 || BGFR_TIMER > 600000)
        {
            BGFR_TIMER                                          = new Long(10000);
        }
        if(BGFR_ID == null)
        {
            BGFR_ID                                             = "ID" + (int)(Math.random()*1000) + Calendar.getInstance().getTime().getTime();
        }

        if(BGFR_MODE.ordinal() < 0 || BGFR_MODE.ordinal() > 1)
            showErrorAndClose("INVALID BGFR MODE PARAMETER, DEVELOPER CHECK");
        else BGFR_MODE                                          = BGFRMode.values()[BGFR_MODE.ordinal()];




    }

    /**
     * Pause processing frames is called when the activity is minimized
     * in this case when the app is minimized, since this is part of whatever
     * module is calling this activity
     */
    private void pauseProcessingFrames()
    {
        if (mDraw != null) {
            mDraw.mStopping = 1;

            // It is essential to limit wait time, because mStopped will not be set to 0, if no frames are feeded to mDraw
            for (int i = 0; i < 100; ++i) {
                if (mDraw.mStopped != 0) break;
                try {
                    Thread.sleep(10);
                } catch (Exception ex) {
                }
            }
        }
    }


    /**
     * Resume processing frames is called when the activity back from onPause
     */
    private void resumeProcessingFrames()
    {
        if (this.mDraw != null)
        {
            this.mDraw.mStopped = 0;
            this.mDraw.mStopping = 0;
        }
    }

    @Override
    public void onPause()
    {
        mActive = false;
        Log.d(TAG, "onPause");
        if(mDraw != null) pauseProcessingFrames();
        super.onPause();
        if (this.mIsFailed) {
            return;
        }
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    @Override
    public void onResume()
    {
        mActive = true;
        Log.d(TAG, "onResume");
        super.onResume();
        if (this.mIsFailed) {
            return;
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(10,"MYAPP:"+TAG);
        if (!this.mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
        }
        resumeProcessingFrames();
    }

    @Override
    protected void onStop() {
        if(mHandler != null && mTimerRunner != null ){
            mHandler.removeCallbacks(mTimerRunner);
        }

        super.onStop();
    }

    public void resetParamsNewLoad(){
        pauseProcessingFrames();
        mDraw.mTracker                                                = new FSDK.HTracker();
        if (FSDK.FSDKE_OK != FSDK.CreateTracker(mDraw.mTracker)) {
            Log.i(TAG,"INVALID TEMPLATE FILE");
        }
        resetTrackerParameters();
        resumeProcessingFrames();
    }

    public void FaceFound(){
        Log.i(TAG, "FaceFound: FACE DETECTED ");
        BGFR_FACEFOUND                                      = true;
    }

    public void StartTimer(){
        mHandler.post(mTimerRunner);
    }

    public void StopTimer(){
        if(MASTER_TIMER  != null) {
            MASTER_TIMER.cancel();
            mHandler.removeCallbacks(mTimerRunner);
        }
    }

    Runnable mTimerRunner = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "Timer: STARTED");
            MASTER_TIMER = new CountDownTimer(BGFR_TIMER,100){
                @Override
                public void onTick(long millisUntilFinished) {
                    int time = (int)(millisUntilFinished/1000);
                    mHandler.post(()->countdownTimer.setText(time / 60 + " : " + time % 60)); //POST HANDLER ON THE CURRENT THREAD FOR ASYNCHRONOUS CONCURRENCY
                }

                @Override
                public void onFinish() {
                    setErrorCode();
                }
            };
            MASTER_TIMER.start();
        }
    };


    @Override
    public void onBackPressed() {
        //DO NOTHING
    }


    public void backPressed(View view){
        Intent intentMessage                                = new Intent();
        intentMessage.putExtra("RESULT",2);
        setResult(Activity.RESULT_OK,intentMessage);
        finish();
    }


    public void switchPressed(View view){
        Log.i(TAG, "onClick: CAMERA BUTTON PRESSED");
        if (currentCamera == 1) {
            currentCamera = 0;
        } else {
            currentCamera = 1;
        }
        this.mPreview.switchToCamera(currentCamera);
        this.mPreview.forceStartPreviewOnSurfaceChange();
        this.mPreview.surfaceChanged(null, 0, 0, 0);
    }

    public void handleFinish(int res){
        Intent intentMessage                                    = new Intent();
        intentMessage.putExtra("RESULT",res);
        setResult(Activity.RESULT_OK,intentMessage);
        finish();
    }

    public void TAKE_PICTURE(){
        if(mDraw != null)
        {
            mDraw.savePicture();
        }
    }

    public void capturePressed(View view){
        captureButton.setVisibility(View.INVISIBLE);
        MyFlow();
        StartTimer();
    }

    public void flashPressed(View view){
        this.mPreview.switchFlash();
    }

    public BGFRMode getBGFR_MODE() {
        return BGFR_MODE;
    }

    public void setBGFR_MODE(BGFRMode mode) {
        if (mDraw != null) mDraw.setMode(mode);
    }

    public static BGFRFlow getBgfrFlow() {
        return BGFR_FLOW;
    }

    public static void setBgfrFlow(BGFRFlow bgfrFlow) {
        BGFR_FLOW = bgfrFlow;
    }

    public String getBGFR_KEY() {
        return BGFR_KEY;
    }

    public void setBGFR_KEY(String BGFR_KEY) {
        this.BGFR_KEY = BGFR_KEY;
    }

    public String getBGFR_ID() {
        return BGFR_ID;
    }

    public void setBGFR_ID(String BGFR_ID) {
        this.BGFR_ID = BGFR_ID;
    }

    public Long getBGFR_TIMER() {
        return BGFR_TIMER;
    }

    public void setBGFR_TIMER(Long BGFR_TIMER) {
        this.BGFR_TIMER = BGFR_TIMER;
    }

    public Boolean getBGFR_FACEFOUND() {
        return BGFR_FACEFOUND;
    }

    public Boolean getBGFR_DETECT_FAKE_FACES() {
        return BGFR_DETECT_FAKE_FACES;
    }

    public void setBGFR_DETECT_FAKE_FACES(Boolean BGFR_DETECT_FAKE_FACES) {
        this.BGFR_DETECT_FAKE_FACES = BGFR_DETECT_FAKE_FACES;
    }

    public Boolean getBGFR_KEEP_FACES() {
        return BGFR_KEEP_FACES;
    }

    public void setBGFR_KEEP_FACES(Boolean BGFR_KEEP_FACES) {
        this.BGFR_KEEP_FACES = BGFR_KEEP_FACES;
    }

    public static String getBgfrPersonTag() {
        return BGFR_PERSON_TAG;
    }

    public static void setBgfrPersonTag(String bgfrPersonTag) {
        BGFR_PERSON_TAG = bgfrPersonTag;
    }

    public void defaultErrorAlert(String s){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setIcon(R.drawable.ic_warning)
                .setTitle("ALERT")
                .setMessage(s.toString())
                .setPositiveButton("DONE",(DialogInterface dialog, int which)->{ dialog.dismiss(); handleFinish(255);})
                .setCancelable(false) ;
        if (!isFinishing()) builder.show();
    }

    /**
     * Abstract Functions which need implementation in the new Class
     *
     */

    public boolean isRealFace(){
        return mDraw.isRealFace();
    }

    public abstract Boolean setDetectFakeFaces();
    public abstract void TrackerSaved();
    public abstract Boolean setKeepFaces();
    public abstract Long setTimer();
    public abstract void TimerFinished();
    public abstract void MyFlow();
    @NonNull
    public abstract  String buttonText();
    @NonNull
    public abstract String headerText();

    /**
     * overideable funcs
     *
     */
    public void customAlert(String s){
        defaultErrorAlert(s);
    }

    public void setErrorCode(){
        if(!getBGFR_FACEFOUND())
        {
          setERROR_CODE(ErrorCodes.KEY_NO_FACE_FOUND);
        }
        else if (getBGFR_FACEFOUND() && isRealFace())
        {
            setERROR_CODE(ErrorCodes.KEY_FACE_NOT_MATCHED);
        }
        else if (getBGFR_FACEFOUND() && !isRealFace()){
            setERROR_CODE(ErrorCodes.KEY_BLINK_NOT_FOUND);
        }
        TimerFinished();

    }

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public void setERROR_CODE(int ERROR_CODE) {
        this.ERROR_CODE = ERROR_CODE;
    }

    @Override
    public void TrackerSavedFirst(){
        if(MASTER_TIMER  != null) {
            MASTER_TIMER.cancel();
            TrackerSaved();

        }
    }
}
