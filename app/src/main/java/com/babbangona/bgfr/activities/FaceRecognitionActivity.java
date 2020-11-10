package com.babbangona.bgfr.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import  com.babbangona.bgfr.activities.VerifyActivity;


import com.babbangona.bgfr.R;
import com.babbangona.bgfr.databinding.ActivityFaceRecognitionBinding;
import com.babbangona.faceapp.util.SharedPreference;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NOTE TO THOMAS: BUTTER KNIFE CANNOT BE USE WITH LIBRARY AND MODULES 
 * SO I AM REFACTORING TO  USE NATIVE ANDROID DATA BINDING
 */

public class FaceRecognitionActivity extends AppCompatActivity {

    //MAIN DATA BINDING OBJECT
    ActivityFaceRecognitionBinding binding;


    public ImageView iv_member_picture;

    //Scan Successful
    
    public LinearLayout linear_layout_scan_success;

    //No Blink
    
    public LinearLayout linear_layout_no_blink;

    //Is there a face in the picture ?
    
    public LinearLayout linear_layout_face_in;

    //Is this the person you want to register ?
    
    public LinearLayout linear_layout_want_to_register;

    //No face in picture Registration is invalid
    
    public LinearLayout linear_layout_noFace;

    //Wrong person Registration is invalid
    
    public LinearLayout linear_layout_wrongface;

    //Verification Failed : Bad lighting
    
    public LinearLayout linear_layout_Bad_lighting;

    //Is this the person in front of you ?
    
    public LinearLayout linear_layout_in_front;

    //Do you want to recapture ?
    
    public LinearLayout linear_layout_recapture;

    //Verification Successful
    
    public LinearLayout linear_layout_bgrf_verifi_success;

    //Blink Cancel
    
    TextView bgfr_no_blink_cancel;

    //Blink Retry
    
    TextView bgfr_no_blink_retry;

    //Face in No
    
    TextView bgfr_face_in_no;

    //Face in Yes
    
    TextView bgfr_face_in_yes;

    //No Face Retry
    
    TextView bgfr_no_face_retry;

    //Want to No
    
    TextView bgfr_want_to_no;

    //want to Yes
    
    TextView bgfr_want_to_yes;

    //want to Register Done
    
    TextView bgfr_wrong_face_done;

    //Scan Success Button
    
    ImageView bgfr_scan_success;




    int state=3;
    private SharedPreference sharedPreference;
    private boolean tries=false;



    private void setBindings(){
        binding = DataBindingUtil.setContentView(this,R.layout.activity_face_recognition);
        iv_member_picture               = binding.ivMemberPicture;
        linear_layout_scan_success      = binding.linearLayoutBgrfScanSuccess;
        linear_layout_no_blink          =  binding.linearLayoutNoBlink;
        linear_layout_face_in           = binding.linearLayoutFaceIn;
        linear_layout_want_to_register  = binding.linearLayoutWantTo;
        linear_layout_noFace            =  binding.linearLayoutNoFace;
        linear_layout_wrongface         = binding.linearLayoutWrongface;
        linear_layout_Bad_lighting      = binding.linearLayoutBadLighting;
        linear_layout_in_front          =  binding.linearLayoutInFront;
        linear_layout_recapture         = binding.linearLayoutRecapture;
        linear_layout_bgrf_verifi_success =  binding.linearLayoutBgrfVerifiSuccess;
        bgfr_no_blink_cancel            =  binding.bgfrNoBlinkCancel;
        bgfr_no_blink_retry             =  binding.bgfrNoBlinkRetry;
        bgfr_face_in_no                 = binding.bgfrFaceInNo;
        bgfr_face_in_yes                = binding.bgfrFaceInYes;
        bgfr_no_face_retry              =  binding.bgfrNoFaceRetry;
        bgfr_want_to_no                 = binding.bgfrWantToNo;
        bgfr_want_to_yes                =  binding.bgfrWantToYes;
        bgfr_wrong_face_done            = binding.bgfrWrongFaceDone;
        bgfr_scan_success               =  binding.bgfrScanSuccess;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBindings();

        sharedPreference=new SharedPreference(this);
        startActivityForResult(new Intent(this, VerifyActivity.class),101);
        showLogic(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 101) {
            Log.d("Face","Result Check");
            if (data != null) {
                if (data.getIntExtra("RESULT", 0) == 1) {
                    //Image image;
                    String image  =sharedPreference.getValue(String.class,"CurrentStaffPicture");
                    setCapturedImage(iv_member_picture,image);//setCapturedImage(iv_member_picture,ccOfficerData.getStaffID(),this,image.getLargeImage());
                    state=4;
                }
                else {
                    state=3;
                }
                if(data.getIntExtra("noblink",0)==51){
                    state=2;
                }
            }
        }

        if (requestCode == 201) {
            Log.d("Face","Result Check");
            if (data != null) {
                if (data.getIntExtra("RESULT", 0) == 1) {
                    //Image image;
                    String image  =sharedPreference.getValue(String.class,"CurrentStaffPicture");
                    setCapturedImage(iv_member_picture,image);//setCapturedImage(iv_member_picture,ccOfficerData.getStaffID(),this,image.getLargeImage());
                    state=4;
                }
                else {
                    state=3;
                }
                if(data.getIntExtra("noblink",0)==51){
                    tries=true;
                    state=2;
                }
            }
        }

        if (requestCode == 301) {
            Log.d("Face","Result Check");
            if (data != null) {
                if (data.getIntExtra("RESULT", 0) == 1) {
                    //Image image;
                    String image  =sharedPreference.getValue(String.class,"CurrentStaffPicture");
                    setCapturedImage(iv_member_picture,image);//setCapturedImage(iv_member_picture,ccOfficerData.getStaffID(),this,image.getLargeImage());
                    state=4;
                }
                else {
                    state=3;
                }
                if(data.getIntExtra("noblink",0)==51){
                    tries=true;
                    state=2;
                }
            }
        }

        if (requestCode == 401) {
            Log.d("Face","Result Check");
            if (data != null) {
                if (data.getIntExtra("RESULT", 0) == 1) {
                    //Image image;
                    String image  =sharedPreference.getValue(String.class,"CurrentStaffPicture");
                    setCapturedImage(iv_member_picture,image);//setCapturedImage(iv_member_picture,ccOfficerData.getStaffID(),this,image.getLargeImage());
                    state=4;
                }
                else {
                    state=3;
                }
                if(data.getIntExtra("noblink",0)==51){
                    tries=true;
                    state=2;
                }
            }
        }
        showLogic(state);
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void setCapturedImage(ImageView iv_member_picture,String new_tracker) {
        this.runOnUiThread(()->{
            if (new_tracker != null){
                Bitmap myBitmap = getBitmap(new_tracker);
                Matrix matrix = new Matrix();
                matrix.preRotate(90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap, 400, 300, true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                iv_member_picture.setImageBitmap(rotatedBitmap);
            }
        });
    }

    private Bitmap getBitmap(String member_picture_byte_array){
        byte[] imageAsBytes = new byte[0];
        if (member_picture_byte_array != null) {
            imageAsBytes = Base64.decode(member_picture_byte_array.getBytes(), Base64.DEFAULT);
        }
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    private void setPicture(ImageView iv_member_picture) {
        File imgFile = new File("staffID.jpg");
        if(imgFile.exists()){
            this.runOnUiThread(()-> {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                iv_member_picture.setImageBitmap(myBitmap);
            });
        }else {
            this.runOnUiThread(()-> {
                iv_member_picture.setImageResource(R.drawable.mask_group);
            });
        }


    }

    public void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    public void hideView(View view) {
        view.setVisibility(View.GONE);
    }

    private void showLogic(int state) {
        if (state == 1){
           //Scan Success
            hideView(linear_layout_Bad_lighting);
            hideView(linear_layout_bgrf_verifi_success);
            hideView(linear_layout_face_in);
            hideView(linear_layout_in_front);
            hideView(linear_layout_no_blink);
            hideView(linear_layout_noFace);
            showView(linear_layout_scan_success);
            hideView(linear_layout_wrongface);
            hideView(linear_layout_want_to_register);
            hideView(linear_layout_recapture);

        }else if (state == 2) {
            //No Blink
            iv_member_picture.setImageResource(R.drawable.bgfrface);
            hideView(linear_layout_Bad_lighting);
            hideView(linear_layout_bgrf_verifi_success);
            hideView(linear_layout_face_in);
            hideView(linear_layout_in_front);
            showView(linear_layout_no_blink);
            hideView(linear_layout_noFace);
            hideView(linear_layout_scan_success);
            hideView(linear_layout_wrongface);
            hideView(linear_layout_want_to_register);
            hideView(linear_layout_recapture);
        }
        else if (state == 3) {
            //Temporary
            hideView(linear_layout_Bad_lighting);
            hideView(linear_layout_bgrf_verifi_success);
            hideView(linear_layout_face_in);
            hideView(linear_layout_in_front);
            hideView(linear_layout_no_blink);
            hideView(linear_layout_noFace);
            hideView(linear_layout_scan_success);
            hideView(linear_layout_wrongface);
            hideView(linear_layout_want_to_register);
            hideView(linear_layout_recapture);

        }
        else if (state == 4) {
            //Is there a face in this picture
            hideView(linear_layout_Bad_lighting);
            hideView(linear_layout_bgrf_verifi_success);
            showView(linear_layout_face_in);
            hideView(linear_layout_in_front);
            hideView(linear_layout_no_blink);
            hideView(linear_layout_noFace);
            hideView(linear_layout_scan_success);
            hideView(linear_layout_wrongface);
            hideView(linear_layout_want_to_register);
            hideView(linear_layout_recapture);

        }
        else if (state == 5) {
            //No Face
            hideView(linear_layout_Bad_lighting);
            hideView(linear_layout_bgrf_verifi_success);
            hideView(linear_layout_face_in);
            hideView(linear_layout_in_front);
            hideView(linear_layout_no_blink);
            showView(linear_layout_noFace);
            hideView(linear_layout_scan_success);
            hideView(linear_layout_wrongface);
            hideView(linear_layout_want_to_register);
            hideView(linear_layout_recapture);
        }
        else if(state==6){
            //Want to Register
            hideView(linear_layout_Bad_lighting);
            hideView(linear_layout_bgrf_verifi_success);
            hideView(linear_layout_face_in);
            hideView(linear_layout_in_front);
            hideView(linear_layout_no_blink);
            hideView(linear_layout_noFace);
            hideView(linear_layout_scan_success);
            hideView(linear_layout_wrongface);
            showView(linear_layout_want_to_register);
            hideView(linear_layout_recapture);
        }

        else if(state==7){
            //Want to Register
            hideView(linear_layout_Bad_lighting);
            hideView(linear_layout_bgrf_verifi_success);
            hideView(linear_layout_face_in);
            hideView(linear_layout_in_front);
            hideView(linear_layout_no_blink);
            hideView(linear_layout_noFace);
            hideView(linear_layout_scan_success);
            showView(linear_layout_wrongface);
            hideView(linear_layout_want_to_register);
            hideView(linear_layout_recapture);
        }
    }



    public void setBgfr_no_blink_cancel(View v){
        startActivity(new Intent(this,FaceRecognitionHomePageActivity.class));
    }


    public void setBgfr_no_blink_retry(View v){
        if(!tries) {
            startActivityForResult(new Intent(this, VerifyActivity.class), 201);
        }
        else {
            showDialogueBoxContactProductSupport();
        }
    }

    //Face No

    public void setBgfr_face_in_no(View v){
        state=5;
        iv_member_picture.setImageResource(R.drawable.no_face);
        showLogic(state);
    }

    //Face Retry
    public void setBgfr_no_face_retry(View v){
        startActivityForResult(new Intent(FaceRecognitionActivity.this, VerifyActivity.class), 401);
    }

    //Face Yes

    public void setBgfr_face_in_yes(View v){
       state=6;
       setPicture(iv_member_picture);
       showLogic(state);
    }

    //Want to register Yes and No and Done
    public void setBgfr_want_to_yes(View v){
        showDialogueBoxAlert();

    }


    public void setBgfr_want_to_no(View v){
        state=7;
        showLogic(state);
    }


    public void setBgfr_wrong_face_done(View v){
        //Back to Face recognition Activity
        startActivity(new Intent(FaceRecognitionActivity.this,FaceRecognitionHomePageActivity.class));
    }

    //Scan Success Button
    public void setBgfr_scan_success(View v){
        //Back to Face recognition Activity
        startActivity(new Intent(FaceRecognitionActivity.this,FaceRecognitionHomePageActivity.class));
    }



    private void showDialogueBoxContactProductSupport() {
        Dialog openDialog = new Dialog(FaceRecognitionActivity.this);
        openDialog.setContentView(R.layout.popup_contact_product_support);
        Button btn=openDialog.findViewById(R.id.txtContent);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(FaceRecognitionActivity.this, VerifyActivity.class), 301);
            }
        });

        Button contact_product_support=openDialog.findViewById(R.id.contact_product_support);

        contact_product_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogueBoxCallIssueCenter();
            }
        });


        if(!openDialog.isShowing()) {
            openDialog.show();
        }

    }

    private void showDialogueBoxCallIssueCenter() {
        Dialog openDialog = new Dialog(FaceRecognitionActivity.this);
        openDialog.setContentView(R.layout.popup_issue_center);
        Button call_product_support=openDialog.findViewById(R.id.call_product_support);
        call_product_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FaceRecognitionActivity.this,FaceRecognitionHomePageActivity.class));
            }
        });

        Button issue_center=openDialog.findViewById(R.id.issue_center);
        issue_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FaceRecognitionActivity.this,FaceRecognitionHomePageActivity.class));
            }
        });
        if(!openDialog.isShowing()) {
            openDialog.show();
        }

    }

    private void showDialogueBoxAlert() {
        Dialog openDialog = new Dialog(FaceRecognitionActivity.this);
        openDialog.setContentView(R.layout.popup_alert);
        openDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button alert_cancel=openDialog.findViewById(R.id.alert_cancel);
        alert_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FaceRecognitionActivity.this,FaceRecognitionHomePageActivity.class));
            }
        });

        Button alert_confirm=openDialog.findViewById(R.id.alert_confirm);
        alert_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state=1;
                showLogic(state);
                openDialog.cancel();
            }
        });
        if(!openDialog.isShowing()) {
            openDialog.show();
        }

    }
}
