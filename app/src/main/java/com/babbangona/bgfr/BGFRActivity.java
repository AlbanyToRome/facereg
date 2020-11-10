package com.babbangona.bgfr;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.AsyncTask;
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
import com.babbangona.bgfr.ProcessImageAndDrawResults.BGFRMode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luxand.FSDK;

import java.util.Calendar;
import java.util.Stack;

import androidx.appcompat.app.AppCompatActivity;

import static com.babbangona.bgfr.BGPermissions.CAMERA_PERMISSION_REQUEST_CODE;

public class BGFRActivity extends AppCompatActivity implements BGCallback{

    //===============INTEGRAL SETTINGS VARIABLES [DEFAULT IF NONE IS RECEIVED]=============//
    //===============STRING CONSTANTS FOR INTENT EXTRAS===================================//
    public static final String PARAMETERS_USER                 = "BGFR_PARAMETERS";
    public static final String PARAMETERS_STRING               = "BGFR_UISTRING";
    public static final String PARAMETERS_MODE                 = "BGFR_MODE";
    public static final String PARAMETERS_FLOW                 = "BGFR_FLOW";
    public static final String PARAMETERS_KEY                  = "BGFR_KEY";
    public static final String PARAMETERS_ID                   = "BGFR_ID";
    public static final String PARAMETERS_TIMER                = "BGFR_TIMER";
    public static final String PARAMETERS_FAKE_FACES           = "BGFR_DETECT_FAKE_FACES";
    public static final String PARAMETERS_KEEP_FACES           = "BGFR_KEEP_FACE_IMAGES";
    public static final String PARAMETERS_PERSON_TAG           = "BGFR_PERSON_TAG";


    //===============STRINGS FROM INTENT EXTRAS=========================================//
    private String   BGFR_PARAMETERS;
    private String   BGFR_UISTRING;
    private BGFRMode BGFR_MODE                                  = BGFRMode.CAPTURE_NEW;
    public static BGFRFlow BGFR_FLOW                            = BGFRFlow.SINGLE_CAPTURE;
    private String   BGFR_KEY;
    private String   BGFR_ID;
    private Long     BGFR_TIMER;
    private Boolean  BGFR_FACEFOUND                             = false;
    private Boolean  BGFR_DETECT_FAKE_FACES                     = false;
    private Boolean  BGFR_KEEP_FACES                            = false;
    public static String   BGFR_PERSON_TAG                      = "ID" + Math.random();
    public static Boolean BGFR_IS_UNBUNDLED                     = false;
    public static Boolean STRICT_CAPTURE_ONLY_MODE              = false;

    private int TEMP_MODE;
    private int TEMP_FLOW;
    //=================================================================================//

    //===============CLASS WORKING VARIABLES==========================================//
    public static float sDensity                                = 1.0F;
    public static final String TAG                              = "BABBANGONA";
    public boolean mActive                                      = false;
    public static int currentCamera                             = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean mIsFailed                                   = false;
    private boolean wasStopped                                  = false;
    private FrameLayout mLayout;
    private PowerManager.WakeLock mWakeLock;
    BGPermissions permissions;
    ProcessImageAndDrawResults mDraw;
    Preview mPreview;
    //===================================================================================//

    //==============UI ELEMENTS WITH BUTTER KNIFE BINDING================================//
    public TextView countdownTimer;
    public Button captureButton;
    //====================================================================================//
    //===========MASTER TIMER===========================================================//
    private CountDownTimer MASTER_TIMER = null;

    //=======Templates=====================================================================
    Stack<String> Trackers;
    private Handler mHandler;
    public static MainRepository repository;
    //====================================================================================//



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doCameraPermissions();


    }

    /**
     * setting up the Views and doing the binding without using the butter knife library
     */
    public void setUpUI(){
        mDraw                                                   = new ProcessImageAndDrawResults(this, this,BGFR_MODE);
        mPreview                                                = new Preview(this, mDraw);
        View localView                                          = LayoutInflater.from(this).inflate(R.layout.capture,null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
       captureButton.setEnabled(false);
    }

    public void attachUI(){
        captureButton                                           = findViewById(R.id.captureButton);
        countdownTimer                                          = findViewById(R.id.timerText);
        if(BGFR_FLOW == BGFRFlow.SINGLE_AUTHENTICATE){
            captureButton.setText("VERIFY FACE");
        }

    }

    /**
     * Sets up permissions for camera:
     * if the user refuses to enable permissions, the activity exits safely
     */
    public void doCameraPermissions(){
       /* permissions                                             = new BGPermissions(this);
        if(permissions.setCameraPermission()){*/
            getPrimeSettings();
            setUpUI();
            begin();
        /*}
        else showErrorAndClose(getString(R.string.error_camera));*/
    }





    /**
     * Gets the integral settings from Intent Put Extra, or defaults, if it doesn't see anything
     * validation for each parameters follows immediately after
     */

    public void getPrimeSettings(){
        mHandler                                                = new Handler();
        repository                                              = new MainRepository(getApplication());
        BGFR_PARAMETERS                                         =  getIntent().getStringExtra(PARAMETERS_USER);
        BGFR_UISTRING                                           =  getIntent().getStringExtra(PARAMETERS_STRING);
        TEMP_MODE                                               =  (getIntent().getIntExtra(PARAMETERS_MODE,0));
        TEMP_FLOW                                               =  (getIntent().getIntExtra(PARAMETERS_FLOW,0));
        BGFR_KEY                                                =  getIntent().getStringExtra(PARAMETERS_KEY);
        BGFR_ID                                                 =  getIntent().getStringExtra(PARAMETERS_ID);
        BGFR_TIMER                                              =  getIntent().getLongExtra(PARAMETERS_TIMER,15000);
        BGFR_DETECT_FAKE_FACES                                  =  getIntent().getBooleanExtra(PARAMETERS_FAKE_FACES,false);
        BGFR_KEEP_FACES                                         =  getIntent().getBooleanExtra(PARAMETERS_KEEP_FACES,false);
        BGFR_PERSON_TAG                                         =  getIntent().getStringExtra(PARAMETERS_PERSON_TAG);

        validateSettings();
    }

    /**
     * Validation of all inputs gotten from the intent extras
     * if any violations occur, then the values are immediately set to default
     */

    public void validateSettings(){

        /*if(BGFR_UISTRING.split("-").length < 2)
        {
            BGFR_UISTRING                                       = "Create ID-Capture";
        }*/


        if(!(isKeyValid())){
            BGFR_KEY                                            = new LuxandKey("").setPredefinedKey().Key;
            Log.i(TAG, "validateSettings: " + BGFR_KEY.substring(0,BGFR_KEY.length()/10));
            if(!isKeyValid())showErrorAndClose("INVALID KEY",0);

        }
        if(BGFR_TIMER < 5000 || BGFR_TIMER > 600000)
        {
            BGFR_TIMER                                          = new Long(10000);
        }
        if(BGFR_ID == null)
        {
            BGFR_ID                                             = "ID" + (int)(Math.random()*1000) + Calendar.getInstance().getTime().getTime();
        }

        if(TEMP_MODE < 0 || TEMP_MODE > 1)
            showErrorAndClose("INVALID BGFR MODE PARAMETER, DEVELOPER CHECK",0);
        else BGFR_MODE                                          = BGFRMode.values()[TEMP_MODE];

        if(TEMP_FLOW < 0 || TEMP_FLOW > 3)
            showErrorAndClose("INVALID BGFR FLOW PARAMETER, DEVELOPER CHECK",0);
        else BGFR_FLOW                                          = BGFRFlow.values()[TEMP_FLOW];

        if(BGFR_PERSON_TAG == null)
            BGFR_PERSON_TAG                                     = "ID" + Math.random();



    }



    public boolean isKeyValid(){
        int res                                                 = FSDK.ActivateLibrary(BGFR_KEY);
        Log.i(TAG, "isKeyValid: BOOLEAN = " + res );
        if(res == 0) return true;
        else return  false;
    }

    public void begin(){
        sDensity                                               = getResources().getDisplayMetrics().scaledDensity;
        FSDK.Initialize();
        mDraw.feature_name                                     = BGFR_ID;
       captureButton.setEnabled(true);

    }

    /**
     * Determine Flow and Begin the process, and countdown timer
     */
    public void determineFlow(){
        switch(BGFR_FLOW){
            case SINGLE_CAPTURE:
                singleCapture();
                break;
            case SINGLE_AUTHENTICATE:
                singleAuth();
                break;
            case AUTHENTICATE_CAPTURE:
               //INVALID STATE TEMPORARILTY TODO: IMPLEMENET FUTURE FUNCTION

            case STACK_CAPTURE:
                String Template = "";
                try{
                    Template                                                 = new getTemplate().execute().get();
                }catch(Exception e){
                    showErrorAndClose("ERROR DB ran into Exception: \n Technical Details : " + e.getLocalizedMessage(),0);
                }
                finally {
                    stackCapture(Template);
                    break;
                }




        }

    }

    public void resetTrackerParameters(){
        int[] arrayOfInt                                        = new int[1];

        if(!BGFR_KEEP_FACES){
            Log.i(TAG, "resetTrackerParameters: KEEP FACES WAS SET TO FALSE, SMALL SIZED TRACKERS");
            FSDK.SetTrackerMultipleParameters(mDraw.mTracker, getResources().getString(R.string.Luxand_Params_Default), arrayOfInt);
            if (arrayOfInt[0] != 0) {
                showErrorAndClose("Error setting tracker parameters 2, position",0);
            }
        }
        else if(BGFR_KEEP_FACES){
            Log.i(TAG, "resetTrackerParameters: KEEP FACES WAS SET TO FALSE, SMALL SIZED TRACKERS");
            FSDK.SetTrackerMultipleParameters(mDraw.mTracker, getResources().getString(R.string.Luxand_Params_Keep_Face), arrayOfInt);
            if (arrayOfInt[0] != 0) {
                showErrorAndClose("Error setting tracker parameters 2, position",0);
            }
        }
        FSDK.SetTrackerMultipleParameters(mDraw.mTracker, "DetectGender=false;DetectExpression=true", arrayOfInt);
        if (arrayOfInt[0] != 0) {
            showErrorAndClose("Error setting tracker parameters 2, position",0);
        }

        // faster smile detection
        FSDK.SetTrackerMultipleParameters(mDraw.mTracker, "AttributeExpressionSmileSmoothingSpatial=0.5;AttributeExpressionSmileSmoothingTemporal=10;", arrayOfInt);
        if (arrayOfInt[0] != 0) {
            showErrorAndClose("Error setting tracker parameters 3, position",0);
        }

    }

    public void singleCapture(){
        mDraw.mode                                              = BGFRMode.CAPTURE_NEW;
        BGFR_FLOW                                               = BGFRFlow.SINGLE_CAPTURE;
        mDraw.mTracker                                          = new FSDK.HTracker();
        if (FSDK.FSDKE_OK != FSDK.CreateTracker(mDraw.mTracker)) {
            showErrorAndClose("INVALID TEMPLATE FILE",0);
        }
        Log.i(TAG, "openCamera: SUCCESS !! FSDK TRACKER CREATED");
        resetTrackerParameters();
        mHandler.post(mTimerRunner);

    }

    public void stackCapture(  String template ){
        mDraw.mode                                              = BGFRMode.AUTHENTICATE;
        BGFR_FLOW                                               = BGFRFlow.STACK_CAPTURE;
        mDraw.mTracker                                          = new FSDK.HTracker();
        if (template == "0" || template.matches("^\0*0\0*?"  ) ||template.length() < 10){
            Log.i(TAG, "stackCapture: EMPTY PILE SENT, TREAT AS A FIRST TIME TRACKER CAPTURE");
            mDraw.mode                                              = BGFRMode.CAPTURE_NEW;
            BGFR_FLOW                                               = BGFRFlow.SINGLE_CAPTURE;
            determineFlow();
        }
        else if(template != null && doExtraTemplateCheck(template)) {
            byte[] bytes                                        = Base64.decode(template,Base64.DEFAULT);
            int res                                             = FSDK.LoadTrackerMemoryFromBuffer(mDraw.mTracker,bytes);
            if (FSDK.FSDKE_OK != res && Trackers.isEmpty()) {
                Log.i(TAG, "authCapture: FAILED");
                showErrorAndClose("CORRUPT OR INVALID TEMPLATE",0);
            }
            else if (FSDK.FSDKE_OK != res && !Trackers.isEmpty()) {
                Log.i(TAG, "authCapture: FAILED");
                showErrorAndClose("CORRUPT OR INVALID TEMPLATE",0);
            }
            resetTrackerParameters();
            mHandler.post(mTimerRunner);
        }


        else
            showErrorAndClose("ERROR INVALID OR NULL TEMPLATE",0);
    }

    public void singleCapture(FSDK.HTracker tracker){
        mDraw.mode                                              = BGFRMode.CAPTURE_NEW;
        BGFR_FLOW                                               = BGFRFlow.STACK_CAPTURE;
        mDraw.mTracker                                          = tracker;
        Log.i(TAG, "openCamera: SUCCESS !! FSDK TRACKER CREATED");
        resetTrackerParameters();
        mHandler.post(mTimerRunner);

    }

    public void singleAuth(){
        mDraw.mode                                              = BGFRMode.AUTHENTICATE;
        BGFR_FLOW                                               = BGFRFlow.SINGLE_AUTHENTICATE;
        mDraw.mTracker                                           = new FSDK.HTracker();

        String template                                         = "";
        try{
            template = new getTemplate().execute().get();
        } catch (Exception e){
            showErrorAndClose("EXCEPTION CAUGHT " + e.getLocalizedMessage(),0);
        }
        Log.i(TAG, "singleAuth: TRACKER STRING  == " + template);
        if(template != null && doExtraTemplateCheck(template)) {
            byte[] bytes                                        = Base64.decode(template,Base64.DEFAULT);
            int res                                             = FSDK.LoadTrackerMemoryFromBuffer(mDraw.mTracker,bytes);
            if (FSDK.FSDKE_OK != res) {
                showErrorAndClose("CORRUPT OR INVALID TEMPLATE",0);
            }
            resetTrackerParameters();
            mHandler.post(mTimerRunner);
        }
        else
            showErrorAndClose("ERROR INVALID OR NULL TEMPLATE",0);

    }

    public Boolean doExtraTemplateCheck(String template){
        /*if(!template.matches("^RlNES.*") )
        {
            Log.i(TAG, "doExtraTemplateCheck: DID NOT MATCH R1NESwY");
            return  false;
        }*/

        if(template.length() < 800)
        {
            Log.i(TAG, "doExtraTemplateCheck: LENGTH LESS THAN 800");
            return  false;
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



    /**
     * Event Listener after Authentication occurs, in authentication
     * and capture mode.
     * Capture Mode notifies Authenticated when capture is done.
     */
    public void Authenticated(){
        switch(BGFR_FLOW){
            case SINGLE_CAPTURE:
                //finish();
               if(!BGFR_DETECT_FAKE_FACES || (BGFR_DETECT_FAKE_FACES && mDraw.isRealFace()))handleExit(false);
                break;
            case SINGLE_AUTHENTICATE:
                //finish();
                if(!BGFR_DETECT_FAKE_FACES || (BGFR_DETECT_FAKE_FACES && mDraw.isRealFace())) handleExit(false);
                break;
            case STACK_CAPTURE:
                if(!BGFR_DETECT_FAKE_FACES || (BGFR_DETECT_FAKE_FACES && mDraw.isRealFace())) handleExit(true);
                break;

        }
    }

    public void TrackerSavedFirst(){
        if (BGFR_MODE == BGFRMode.CAPTURE_NEW && (!BGFR_DETECT_FAKE_FACES || (BGFR_DETECT_FAKE_FACES && mDraw.isRealFace()))){
            handleExit(false);
        }

    }



    /**
     * Show Error Alert Dialog and Close
     */
    public void showErrorAndClose(String Message, int result){
        pauseProcessingFrames();
        if(MASTER_TIMER != null) MASTER_TIMER.cancel();
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered)
                .setIcon(R.drawable.ic_warning)
                .setTitle("ALERT")
                .setMessage(Message)
                .setCancelable(false)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: SOME ADVANCED LOGGING AND SETTING ON INTENT EXTRAS TO PASS BACK
                        Intent intentMessage                                = new Intent();
                        intentMessage.putExtra("RESULT",result);
                        setResult(Activity.RESULT_OK,intentMessage);
                        finish();
                    }
                }).show();
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

    /**
     * 1. On pause calls pause Processing frames method
     * 2. On resume calls pause Resume frames method
     * 3. on Stop closes the activity safely
     */

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


    protected void onStop()
    {
        mHandler.removeCallbacks(mTimerRunner);
        if(MASTER_TIMER != null) MASTER_TIMER.cancel();
        mActive = false;
        Log.d(TAG, "onStop");
        if (this.mIsFailed) {
            return;
        }
        if ((this.mDraw != null) || (this.mPreview != null))
        {
            this.mPreview.setVisibility(View.GONE);
            this.mLayout.setVisibility(View.GONE);
            this.mLayout.removeAllViews();
            this.mPreview.releaseCallbacks();
            this.mPreview = null;
            this.mDraw = null;
            this.wasStopped = true;
        }
        super.onStop();
    }

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


    public void capturePressed(View view){
       captureButton.setVisibility(View.INVISIBLE);
        determineFlow();
    }

    Runnable mTimerRunner = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "Timer: STARTED");
            MASTER_TIMER = new CountDownTimer(BGFR_TIMER,900){
                @Override
                public void onTick(long millisUntilFinished) {
                   int time = (int)(millisUntilFinished/1000);
                       countdownTimer.setText( time/60 + " : " + time%60 );
                }

                @Override
                public void onFinish() {
                            nextActionMode();
                }

                public void nextActionMode() {

                    if (!BGFR_FACEFOUND) showErrorAndClose("NO FACE DETECTED",2);
                    else if(!BGFR_DETECT_FAKE_FACES || (BGFR_DETECT_FAKE_FACES && mDraw.isRealFace())) {
                        switch (BGFR_FLOW) {
                            case SINGLE_CAPTURE:
                                handleExit(false);
                                break;
                            case SINGLE_AUTHENTICATE:
                                failedFinished();
                                break;
                            case STACK_CAPTURE:
                                if (!BGFR_FACEFOUND) showErrorAndClose("NO FACE DETECTED",2);
                                else {
                                    BGFR_FACEFOUND = false;
                                    singleCapture(mDraw.mTracker);
                                }
                                break;
                        }
                    }

                    else
                    {
                        showErrorAndClose("CANNOT DETERMINE IF FACE IS REAL, PLEASE OPEN AND CLOSE EYES AT SHORT INTERVALS",0);
                    }
                }
            };
            MASTER_TIMER.start();
        }
    };

    /**
     * Permissions result function for first time users
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                getPrimeSettings();
                setUpUI();
                begin();
                break;
            default:
                break;
        }
    }

    /**
     * Handle the exiting, primarily to handle if the user of the module wanted to detect fake faces
     */
    public void failedFinished(){
        Intent intentMessage                                    = new Intent();
        intentMessage.putExtra("RESULT",0);
        setResult(Activity.RESULT_OK,intentMessage);
        finish();
    }

    public void handleExit(Boolean isAuthCaptureFraud){

        if(isAuthCaptureFraud){
            //showErrorAndClose("FACE ALREADY CAPTURED");
            Intent intentMessage                                    = new Intent();
            intentMessage.putExtra("RESULT",0);
            setResult(Activity.RESULT_OK,intentMessage);
            finish();
        }
        else{
            if((BGFR_DETECT_FAKE_FACES && mDraw.isRealFace()) || !BGFR_DETECT_FAKE_FACES)
            {
                Intent intentMessage                                    = new Intent();
                intentMessage.putExtra("RESULT",1);
                setResult(Activity.RESULT_OK,intentMessage);
                finish();
            }
            else
            {
                showErrorAndClose("EXITING: CANNOT DETERMINE IF FACE IS REAL, PLEASE CLOSE, AND OPEN EYES AT SHORT INTERVALS",0);
            }
        }


    }





    private class getTemplate extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            String res = repository.getFace().getTemplate();
            return res.trim();
        }
    }

    public void FaceFound(){
        BGFR_FACEFOUND                                      = true;
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
}
