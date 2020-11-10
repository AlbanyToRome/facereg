package com.babbangona.bgfr.activities;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.babbangona.bgfr.CustomLuxandActivity;
import com.babbangona.bgfr.Database.BGFRInfo;
import com.babbangona.bgfr.ErrorCodes;
import com.babbangona.bgfr.R;
import com.babbangona.faceapp.util.SharedPreference;

public class VerifyActivity extends CustomLuxandActivity {
    private SharedPreference sharedPreference;
    @Override
    public Boolean setDetectFakeFaces() {
        return true;
    }

    @Override
    public void TrackerSaved() {
        BGFRInfo info = new BGFRInfo(this);
        String largeImage = info.getBiggerImageString();
        Log.v("LargeImage", "------>" + largeImage);

        sharedPreference.putValue("CurrentStaffPicture",largeImage);
        Intent intentMessage = new Intent();
        intentMessage.putExtra("RESULT", 1);
        this.setResult(Activity.RESULT_OK, intentMessage);
        this.finish();

    }

    @Override
    public Boolean setKeepFaces() {
        return true;
    }

    @Override
    public Long setTimer() {
        return 15000L;
    }

    @Override
    public void TimerFinished() {

        switch(getERROR_CODE())
        {
            case ErrorCodes.KEY_GENERIC_ERROR:
                //GENERIC ERROR: NOT LIKELY TO HAPPEN
                break;
            case ErrorCodes.KEY_NO_FACE_FOUND:
                //NO FACE FOUND DECIDE WHAT TO DO
                //showErrorAndClose(this.getResources().getString(R.string.no_face_found));
                Intent noFaceIntent = new Intent();
                noFaceIntent.putExtra("noblink",51);
                this.setResult(Activity.RESULT_OK,noFaceIntent);
                this.finish();
                break;
            case ErrorCodes.KEY_BLINK_NOT_FOUND:
                //NO BLINK FOUND DECIDE WHAT TO DO
                Intent intentMessage = new Intent();
                intentMessage.putExtra("noblink",51);
                this.setResult(Activity.RESULT_OK,intentMessage);
                this.finish();
                showErrorAndClose(this.getResources().getString(R.string.blink_not_found));
                break;
            case ErrorCodes.KEY_FACE_NOT_MATCHED:
                //NO FACE MATCHED DECIDE WHAT TO DO
                showErrorAndClose(this.getResources().getString(R.string.face_not_matched));
                break;
        }

    }

    @Override
    public void MyFlow() {
        sharedPreference=new SharedPreference(this);
        loadBlankTracker("StaffID");
    }

    @NonNull
    @Override
    public String buttonText() {
        return getString(R.string.capture);
    }

    @NonNull
    @Override
    public String headerText() {
        return getString(R.string.capture_id);
    }

    @Override
    public void Authenticated() {

    }
}
