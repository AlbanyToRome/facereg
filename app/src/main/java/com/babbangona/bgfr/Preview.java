package com.babbangona.bgfr;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Process;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.List;

class Preview extends SurfaceView implements Callback {
    public static final String TAG                              = "BABBANGONA";

    /**
     *__________________________________________________
     *__________________________________________________
     *_____________N_:_________________;:_N_____________
     *_____________d.                    .d_____________
     *____________O'                      'O____________
     *____KKKKKKKk_                        _kKKKKKKK____
     *Ko_........                            ........_oK
     ||'                 ._cod____doc_.                 '||
     ||               .;dKN__________NKd;.       'c:.    ||
     ||              _kN_____0Okkk0_____Nk_     .dNK;    ||
     ||             c____N_:..    ..:_N____c     .'.     ||
     ||            :____0:            :0____:            ||
     ||           ._____:              :_____.           ||
     ||          .O___0'              '0___O.            ||
     ||           ._____:              :_____.           ||
     ||            :____0:            :0____:            ||
     ||             c____N_:..    ..:_N____c             ||
     ||              ;kN_____0kkkk0_____Nk_              ||
     ||               .;dKN__________NKd;.               ||
     ||                  ._cod____doc_.                  ||
     ||.                                                .||
     *k;.                                            .;k
     *__KOkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkOK__
     *__________________________________________________
     *__________________________________________________
     */

    /**
     * Class Variables as adapted from the Luxand sample Code
     * 1. Camera                                - for accessing the camera for correct rendering in the surface view
     * 2. ProcessImageAndDrawResults            - for processing images obtained from the preview callback
     * 3. finished                              - if preview is completed
     * 4. SurfaceHolder                         - surface holder for rendering previews as required the android OS when using the camera
     * 5. isCameraOpen                          - As a mutex resource, preview class has to ensure that the camera isn't open before using it
     * 6. isPreviewStarted                      - if preview is already being rendered then initialization is not needed
     */
    public Camera mCamera;
    Context mContext;
    ProcessImageAndDrawResults mDraw;
    boolean mFinished;
    SurfaceHolder mHolder;
    boolean mIsCameraOpen                                       = false;
    boolean mIsPreviewStarted                                   = false;

    /**
     *
     * sets Class Variables, and adds the class  as a callback to the surface holder
     * @param paramContext, context for accessing resources which require it
     * @param paramProcessImageAndDrawResults for recieving previews to pass on to the luxand SDK and use for further processing
     */
    Preview(Context paramContext, ProcessImageAndDrawResults paramProcessImageAndDrawResults)
    {
        super(paramContext);
        this.mContext                                           = paramContext;
        this.mDraw                                              = paramProcessImageAndDrawResults;
        this.mHolder                                            = getHolder();
        this.mHolder.addCallback(this);
        this.mHolder.setType(3);
    }


    /**
     * Resize appropriately according to the camera properties
     * @param paramFloat
     */
    private void makeResizeForCameraAspect(float paramFloat)
    {
        ViewGroup.LayoutParams localLayoutParams                = getLayoutParams();
        int i                                                   = getWidth();
        int j                                                   = (int)(i / paramFloat);
        if (j != localLayoutParams.height)
        {
            localLayoutParams.height                            = j;
            localLayoutParams.width                             = i;
            setLayoutParams(localLayoutParams);
            invalidate();
        }
    }

    public void forceStartPreviewOnSurfaceChange()
    {
        this.mIsPreviewStarted                                  = false;
    }

    public void releaseCallbacks()
    {
        if (this.mCamera != null) {
            this.mCamera.setPreviewCallback(null);
        }
        if (this.mHolder != null) {
            this.mHolder.removeCallback(this);
        }
        this.mDraw = null;
        this.mHolder = null;
    }

    /**
     * On surface change call back to implement the suface again during change e.g after on resume
     * @param paramSurfaceHolder
     * @param paramInt1
     * @param paramInt2
     * @param paramInt3
     */
    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3)
    {
        Log.d(TAG, "surfaceChanged");
        if (this.mCamera == null) {
            return;
        }
        if (this.mIsPreviewStarted)
        {
            Log.d(TAG, "surfaceChanged: Preview already started");
            return;
        }
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters                        = mCamera.getParameters();

        //Keep uncommented to work correctly on phones:
        //This is an undocumented although widely known feature
        /**/
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            parameters.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90); // For Android 2.2 and above
            mDraw.rotated                                   = true;
        } else {
            parameters.set("orientation", "landscape");
            mCamera.setDisplayOrientation(0); // For Android 2.2 and above
        }
        /**/

        // choose preview size closer to 640x480 for optimal performance
        List<Size> supportedSizes = parameters.getSupportedPreviewSizes();
        int width                                           = 0;
        int height                                          = 0;
       /* for (Size s: supportedSizes) {
            Log.i(TAG, "surfaceChanged: " +  s.width + " || " + s.height);
            if ((width - 640)*(width - 640) + (height - 480)*(height - 480) >
                    (s.width - 640)*(s.width - 640) + (s.height - 480)*(s.height - 480)) {
                width = s.width;
                height = s.height;
            }
        }*/
        //TODO: TEST THIS ALTERNATIVE AS LARGEST SIZE
        width                                               = supportedSizes.get(supportedSizes.size()-1).width;
        height                                              = supportedSizes.get(supportedSizes.size()-1).height;
        Log.i(TAG, "surfaceChanged: " +  width + " || " + height);

        //try to set preferred parameters
        try {
            if (width*height > 0) {
                parameters.setPreviewSize(width, height);
            }
            //parameters.setPreviewFrameRate(10);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            mCamera.setParameters(parameters);
        } catch (Exception ex) {
        }

        if (!mIsPreviewStarted) {
            mCamera.startPreview();
            mIsPreviewStarted = true;
        }

        parameters = mCamera.getParameters();
        Camera.Size previewSize = parameters.getPreviewSize();
        makeResizeForCameraAspect(1.0f / ((1.0f * previewSize.width) / previewSize.height));
    }

    public void surfaceCreated(SurfaceHolder paramSurfaceHolder)
    {
        Log.d(TAG, "surfaceCreated");
        if (this.mIsCameraOpen) {
            return;
        }
        this.mIsCameraOpen = true;
        this.mFinished = false;
        switchToCamera();
    }

    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder)
    {
        Log.d(TAG, "surfaceDestroyed");
        this.mFinished = true;
        if (this.mCamera != null)
        {
            this.mCamera.setPreviewCallback(null);
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }
        this.mIsCameraOpen = false;
        this.mIsPreviewStarted = false;
    }


    public void switchToCamera()
    {
        switchToCamera(BGFRActivity.currentCamera);
    }

    /**
     * Switches the camera from the front camera to the back camera
     * vice versa
     */
    public void switchToCamera(int paramInt)
    {
        this.mDraw.cameraType = paramInt;
        if (this.mCamera != null)
        {
            this.mCamera.setPreviewCallback(null);
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }
        this.mDraw.switchingCamera = true;
        CameraInfo localCameraInfo = new CameraInfo();
        int i = 0;
        int k = 0;
        int j = k;
        while (i < Camera.getNumberOfCameras())
        {
            Camera.getCameraInfo(i, localCameraInfo);
            if (localCameraInfo.facing == paramInt)
            {
                k = 1;
                j = i;
            }
            i += 1;
        }
        if (k != 0) {
            this.mCamera = Camera.open(j);
        } else {
            this.mCamera = Camera.open();
        }
        try
        {
            this.mCamera.setPreviewDisplay(getHolder());
            this.mCamera.setPreviewCallback(new PreviewCallback()
            {
                public void onPreviewFrame(byte[] paramAnonymousArrayOfByte, Camera paramAnonymousCamera)
                {
                    if (Preview.this.mDraw != null)
                    {
                        if (Preview.this.mFinished) {
                            return;
                        }
                        if (Preview.this.mDraw.mYUVData == null)
                        {
                            // Initialize the draw-on-top companion
                            Camera.Parameters params = paramAnonymousCamera.getParameters();
                            mDraw.mImageWidth = params.getPreviewSize().width;
                            mDraw.mImageHeight = params.getPreviewSize().height;
                            mDraw.mRGBData = new byte[3 * mDraw.mImageWidth * mDraw.mImageHeight];
                            mDraw.mYUVData = new byte[paramAnonymousArrayOfByte.length];
                        }
                        System.arraycopy(paramAnonymousArrayOfByte, 0, Preview.this.mDraw.mYUVData, 0, paramAnonymousArrayOfByte.length);
                        Preview.this.mDraw.invalidate();
                    }
                }
            });
        }
        catch (Exception localException)
        {
            // for (;;) {}
            new AlertDialog.Builder(this.mContext).setMessage("Cannot open camera").setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
                {
                    Process.killProcess(Process.myPid());
                }
            }).show();
            if (this.mCamera != null)
            {
                this.mCamera.release();
                this.mCamera = null;
            }
        }

    }
}
