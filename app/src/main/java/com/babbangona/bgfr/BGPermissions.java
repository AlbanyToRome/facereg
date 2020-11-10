package com.babbangona.bgfr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * PERMISSIONS MANAGER TO FREE UP THE MAIN CLUTTER
 * FROM BEING CLUTTERED WITH PERMISSIONS
 *
 *____########_____________________________
 *___#_______##____________________________
 *_######______##__________________________
 *_##____#______###________________________
 *_##____#_________########################
 *_##____#_______#######________###___###__
 *_###__#______##__________________________
 *__###______##____________________________
 *____########_____________________________
 */

//TODO: ADD OTHER PERMISSIONS HERE [DO NOT CLUTTER UI THREAD WITH PERMISSION REQUESTS]
public class BGPermissions  {
        private Context context;
        public static  final int CAMERA_PERMISSION_REQUEST_CODE                = 231;

        public BGPermissions(@NonNull Context mContext){
            context                                                            = mContext;
            setCameraPermission();
        }

        public boolean setCameraPermission(){
            ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.WAKE_LOCK}, CAMERA_PERMISSION_REQUEST_CODE);
                return  true;

        }

        /**
         * Boolean method to determine if Camera permission has been granted
         * already by the phone
         * @return
         */
        public boolean CameraPermissionGranted() {
            return
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            ==
                            PackageManager.PERMISSION_GRANTED);
        }



}

