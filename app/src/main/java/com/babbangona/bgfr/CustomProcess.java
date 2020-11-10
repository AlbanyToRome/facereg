package com.babbangona.bgfr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.babbangona.bgfr.Database.Face;
import com.babbangona.bgfr.Database.MainRepository;
import com.luxand.FSDK;
import com.luxand.FSDK.FSDK_Features;
import com.luxand.FSDK.FSDK_IMAGEMODE;
import com.luxand.FSDK.HImage;
import com.luxand.FSDK.HTracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.AsynchronousCloseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.babbangona.bgfr.CustomLuxandActivity.TAG;

/**
 * PROCESSES IMAGES FROM CAMERA AND PASSES IT ONTO LUXAND
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
 *
 *                      || ||
 *                      || ||
 *                      || ||
 *                      || ||
 *                   ====    ====
 *                   \          /
 *                     \      /
 *                      \    /
 *                       \ /
 *
 *      =======================================
 *      ||             ======                 ||
 *      ||            /      \                ||
 *      ||            |o    o|                ||
 *      ||            \  __  /                ||
 *      ||     _______/====== \_______        ||
 *      ||    |                        |      ||
 *      ||    |                        |      ||
 *      ||====================================||
 *
 */

class CustomProcess extends View {

    //================THREAD HANDLER====================
    private Handler mHandler;

    //========BOOLEANS====================
    public volatile boolean switchingCamera;
    boolean first_frame_saved;
    public boolean mShowName                                        = false;
    boolean rotated                                                 = false;
    boolean named                                                   = false;
    private boolean mIsShowingFps                                   = false;
    private boolean faceFound                                       = false;
    private boolean detectFakeFaces                                 = false;
    private boolean SAVED_IMAGE                                     = false;

    //========INTEGERS====================

    public int cameraType;
    int mImageHeight;
    int mImageWidth;
    final int MAX_FACES                                             = 1;
    private int mFrameCount                                         = 0;
    private BGFRMode mode;
    int mStopped                                                    = 0;
    int mStopping                                                   = 0;
    int mTouchedIndex                                               = -1;

    //========PAINT======================
    Paint mPaintBlue;
    Paint mPaintBlueTransparent;
    Paint mPaintGreen;

    //========BYTE ARRAY=================
    byte[] mRGBData;
    byte[] mYUVData;

    //=======MISCELLANEOUS================

    long mTouchedID;
    public long face_count[];
    public HTracker mTracker;
    private HImage RotatedImage;
    Context mContext;
    final Lock faceLock                                         = new ReentrantLock();
    final FaceRectangle[] mFacePositions                        = new FaceRectangle[5];
    public String feature_name                                  = CustomLuxandActivity.BGFR_PERSON_TAG;
    final long[] mIDs                                           = new long[5];

    //==========CALLBACK [IMPORTANT]=========
    private BGCallback event;

    //==========EXPRESSION DETECTION VALUE AND ARRAY LIST=========
    private int XconfidenceEyesOpenPercent                      = 0;
    public ArrayList<Integer> DetectedExpressions               = new ArrayList<>(0);

    //=====================IMAHE MODE OF THE LUXAND IMAGE HANDLER==========
    private FSDK_IMAGEMODE imagemode;

    //=========================FACE VARIABLE FOR STRORING THE TRACKER================
    private Face mFace = new Face();

    //======================IF FACE AND NO BLINK=========================
    private boolean noBlink                         =   true;

    public boolean isNoBlink() {
        return noBlink;
    }

    public void setNoBlink(boolean noBlink) {
        this.noBlink = noBlink;
    }

    public CustomProcess(Context paramContext, BGCallback call, BGFRMode CaptureMode) {

        super(paramContext);
        this.event                                              = call;
        this.mode                                               = CaptureMode;
        this.mContext                                           = paramContext;
        this.mYUVData                                           = null;
        this.mRGBData                                           = null;
        this.first_frame_saved                                  = false;
        this.mPaintBlueTransparent                              = new Paint();
        this.mPaintGreen                                        = new Paint();
        this.mPaintBlue                                         = new Paint();
        this.mHandler                                           = new Handler();
        this.mPaintGreen.setStyle(Style.STROKE);
        this.mPaintGreen.setColor(-16711936);
        this.mPaintGreen.setStrokeWidth(4.0F);
        this.mPaintGreen.setTextSize(CustomLuxandActivity.sDensity * 20.0F);
        this.mPaintGreen.setTextAlign(Align.CENTER);
        this.mPaintBlue.setStyle(Style.FILL);
        this.mPaintBlue.setColor(-16776961);
        this.mPaintBlue.setTextSize(CustomLuxandActivity.sDensity * 20.0F);
        this.mPaintBlue.setTextAlign(Align.CENTER);
        this.mPaintBlueTransparent.setStyle(Style.STROKE);
        this.mPaintBlueTransparent.setStrokeWidth(2.0F);
        this.mPaintBlueTransparent.setColor(-16776961);
        this.mPaintBlueTransparent.setTextSize(18.0F * CustomLuxandActivity.sDensity);


    }


    /**
     * Pre Image processing:
     * After getting the image from the camera in the tradition YUV420SP Format
     * It is more useful do a conversion to RGB, to get a 3 dimensional Tensor Image
     * which the Luxand FSDK can then process with ease for the Image Detection
     *
     * @param rgb      The RGB 3 dimensional Image Tensor
     * @param yuv420sp The YUV420SP Raw data from the Camera
     * @param width    width of the camera preview
     * @param height   height of the camera Preview
     */
    public void decodeYUV420SP(byte[] rgb, byte[] yuv420sp, int width, int height) {
        setDecodeYUV420SRunnable(rgb,yuv420sp,width,height);
    }

    private void setDecodeYUV420SRunnable(byte[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        int yp = 0;
        for (int j = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[3 * yp] = (byte) ((r >> 10) & 0xff);
                rgb[3 * yp + 1] = (byte) ((g >> 10) & 0xff);
                rgb[3 * yp + 2] = (byte) ((b >> 10) & 0xff);
                ++yp;
            }
        }
    }


    /**
     * This function uses the facial features requested from the calling function to get the face rectangle dimensions
     * several features may include
     * +Eye position
     * +Nose Position
     * +Mouth Position
     * etc
     * It takes in the FSDK Features, and returns the rectangle coordinates (C++ Style, but in Java's weird way)
     * @param Features The Luxand Facial Features Detected
     * @param fr the resulting facial rectangle coordinates
     * @return
     */
    int GetFaceFrame(FSDK.FSDK_Features Features, FaceRectangle fr)
    {
        if (Features != null && fr != null) {


            float u1 = Features.features[0].x;
            float v1 = Features.features[0].y;
            float u2 = Features.features[1].x;
            float v2 = Features.features[1].y;
            float xc = (u1 + u2) / 2;
            float yc = (v1 + v2) / 2;
            int w = (int) Math.pow((u2 - u1) * (u2 - u1) + (v2 - v1) * (v2 - v1), 0.5);

            fr.x1 = (int) (xc - w * 1.6 * 0.9);
            fr.y1 = (int) (yc - w * 1.1 * 0.9);
            fr.x2 = (int) (xc + w * 1.6 * 0.9);
            fr.y2 = (int) (yc + w * 2.1 * 0.9);
            if (fr.x2 - fr.x1 > fr.y2 - fr.y1) {
                fr.x2 = fr.x1 + fr.y2 - fr.y1;
            } else {
                fr.y2 = fr.y1 + fr.x2 - fr.x1;
            }
            return 0;
        }
        return FSDK.FSDKE_INVALID_ARGUMENT;
    }

    /**
     * Processing the Face frame to draw Rectangles on the surface holder canvas
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {

        if (mStopping == 1)
        {
            mStopped = 1;
            super.onDraw(canvas);
            return;
        }

        if (this.switchingCamera)
        {
            FSDK.SetTrackerParameter(this.mTracker, "VideoFeedDiscontinuity", "0");
            this.switchingCamera = false;
        }

        if (mYUVData == null || mTouchedIndex != -1)
        {
            super.onDraw(canvas);
            return;
        }

        int canvasWidth                                                 = canvas.getWidth();
        decodeYUV420SP(mRGBData, mYUVData, mImageWidth, mImageHeight);
        if(mRGBData == null) return;


        // Load image to FaceSDK
        HImage Image                                                    = new HImage();
        imagemode                                                       = new FSDK_IMAGEMODE();
        imagemode.mode                                                  = FSDK_IMAGEMODE.FSDK_IMAGE_COLOR_24BIT;
        try{
           FSDK.LoadImageFromBuffer(Image, mRGBData, mImageWidth, mImageHeight, mImageWidth * 3, imagemode);
        }catch(Exception e){
            Log.i(TAG, "saveImageToDir: EXCEPTION CAUGHT IN LOAD IMAGE FROM BUFFER ASYNC TASK :  " + e.getMessage());
        }
        if (this.cameraType == 1) {
            FSDK.MirrorImage(Image, false);
        }
        RotatedImage                                                    = new HImage();
        FSDK.CreateEmptyImage(RotatedImage);

        /**it is necessary to work with local variables
         *  (onDraw called not the time when mImageWidth,... being reassigned,
         *  so swapping mImageWidth and mImageHeight may be not safe)
         */
        int ImageWidth = mImageWidth;

        if (rotated)
        {
            ImageWidth = mImageHeight;
            if (this.cameraType == 1) {
                FSDK.RotateImage90(Image, -1, RotatedImage);
            } else {
                FSDK.RotateImage90(Image, 1, RotatedImage);
            }
        }
        else
        {
            FSDK.CopyImage(Image, RotatedImage);
        }
        FSDK.FreeImage(Image);



        long IDs[]                                                      = new long[MAX_FACES];
        face_count                                                      = new long[1];
        FSDK.FeedFrame(mTracker, 0 , RotatedImage, face_count, IDs);

        FSDK.FreeImage(RotatedImage);
        faceLock.lock();

        for (int i = 0; i < MAX_FACES; ++i) {
            mFacePositions[i]                                           = new FaceRectangle();
            mFacePositions[i].x1                                        = 0;
            mFacePositions[i].y1                                        = 0;
            mFacePositions[i].x2                                        = 0;
            mFacePositions[i].y2                                        = 0;
            mIDs[i]                                                     = IDs[i];
        }

        float ratio                                                     =  (canvasWidth * 1.0f) / ImageWidth;
        if(face_count[0] > 0) event.FaceFound();
        for (int i = 0; i < (int) face_count[0]; ++i) {
            FSDK_Features Eyes                                          = new FSDK_Features();
            FSDK.GetTrackerEyes(mTracker, 0, mIDs[i], Eyes);

            GetFaceFrame(Eyes, mFacePositions[i]);

            //======EXPRESSION DETECTION AND CALCULATION
            String values[] = new String[3];
            FSDK.GetTrackerFacialAttribute(mTracker, 0, IDs[i], "Expression", values, 1024);
            float[] confidenceEyesOpen                                  = new float[4];
            FSDK.GetValueConfidence(values[0], "EyesOpen", confidenceEyesOpen);
            int confidenceEyesOpenPercent                               = (int)(confidenceEyesOpen[0] * 100);
            XconfidenceEyesOpenPercent                                  = confidenceEyesOpenPercent;
            DetectedExpressions.add(XconfidenceEyesOpenPercent);
            Log.i(TAG, "onDraw: DETECTED EXPRESSION, EYE OPEN PERCENT " + XconfidenceEyesOpenPercent);
            //==============================================================
            mFacePositions[i].x1                                        *= ratio;
            mFacePositions[i].y1                                        *= ratio;
            mFacePositions[i].x2                                        *= ratio;
            mFacePositions[i].y2                                        *= ratio;
        }

        faceLock.unlock();

        int shift =(int)(22 * CustomLuxandActivity.sDensity);

        // Mark and name faces
        for (int i = 0; i < face_count[0]; ++i)
        {
            canvas.drawRect(mFacePositions[i].x1, mFacePositions[i].y1, mFacePositions[i].x2, mFacePositions[i].y2, mPaintGreen);
            int mid  = mFacePositions[i].x1 + (mFacePositions[i].x2 - mFacePositions[i].x1 )/2;
            canvas.drawText(" Eye Track - " + XconfidenceEyesOpenPercent, mid , mFacePositions[i].y1, mPaintGreen);
            if (IDs[i] != -1 )
            {
                String names[]                                          = new String[4];
                FSDK.GetAllNames(mTracker, IDs[i], names, 2048);
                if (names[0] != null && names[0].length() > 0  &&  mode == BGFRMode.AUTHENTICATE) {
                    Log.i(TAG, "onDraw: NAME  = "+ names[0]);

                        if(!isDetectFakeFaces() || (isDetectFakeFaces() && isRealFace()))
                            event.Authenticated();

                    canvas.drawText(names[0].substring(0,names[0].length()/10), mid , mFacePositions[i].y2 , mPaintBlue);
                }
                else if (names[0] == null && mode == BGFRMode.AUTHENTICATE) {
                    canvas.drawText("NO MATCH", mid , mFacePositions[i].y2, mPaintBlue);

                }
                else if(mode == BGFRMode.CAPTURE_NEW)
                {
                    if(!isDetectFakeFaces() || (isDetectFakeFaces() && isRealFace()))
                        PreSaveTracker();
                    //TODO: DETERMINE IF IN CAPTURE MODE AND CALL SAVE FEATURE FUNCTION
                }

                if(detectFakeFaces)
                {

                }

            }

        }

        super.onDraw(canvas);
    }

    /**
     * Procedure taken before calling the save tracker method
     * The FSDK face operation is locked, and the lock and unlock ID are then set before proceeding to save tracker
     * Implemented as advised by the Luxand Sample, and Documentation.
     *
     */
    public void PreSaveTracker() {

        int x,y;
        faceLock.lock();
        FaceRectangle rects[]                                           = new FaceRectangle[MAX_FACES];
        long IDs[]                                                      = new long[MAX_FACES];
        for (int i = 0; i < MAX_FACES; ++i) {
            rects[i]                                                    = new FaceRectangle();
            rects[i].x1                                                 = mFacePositions[i].x1;
            rects[i].y1                                                 = mFacePositions[i].y1;
            rects[i].x2                                                 = mFacePositions[i].x2;
            rects[i].y2                                                 = mFacePositions[i].y2;
            IDs[i]                                                      = mIDs[i];
        }
        faceLock.unlock();
        x                                                               = (rects[0].x1 + rects[0].x2) / 2;
        y                                                               = (rects[0].y1 + rects[0].y2) / 2;

        for (int i = 0; i < 1; ++i)
        {
            if (rects[i] != null && rects[i].x1 <= x && x <= rects[i].x2 && rects[i].y1 <= y && y <= rects[i].y2 + 30) {
                mTouchedID                                              = IDs[i];
                mTouchedIndex                                           = i;
                FSDK.LockID(mTracker, mTouchedID);
                FSDK.SetName(mTracker, mTouchedID, feature_name);
                if (feature_name.length() <= 0)
                    FSDK.PurgeID(mTracker, mTouchedID);
                FSDK.UnlockID(mTracker, mTouchedID);
                mTouchedIndex                                           = -1;
                if(mode == BGFRMode.CAPTURE_NEW )
                {
                    if(!isDetectFakeFaces()){
                        saveTracker();
                    }
                    else if(isDetectFakeFaces() && isRealFace()){
                        saveTracker();
                    }
                }
            }
        }
    }

    /**
     * turns the FSDK tacker file to a byte stream, and then to string
     * the size is not predetermined, and depends on the tracker size
     * @return String of the template byte stream is successful
     */
    public  void saveTracker(){
        Log.i(TAG, "saveTracker: IN SAVE TRACKER FUNCTION");
        long [] bufferSize                                              =  new long[8];
        FSDK.GetTrackerMemoryBufferSize(mTracker,bufferSize);
        byte[] bytes                                                    = new byte[(int)bufferSize[0]];
        savePicture();
        if ( mFace.getTemplate().length() < 1000 && mFace.getSingleTemplate().length() < 1000) {
            if (FSDK.SaveTrackerMemoryToBuffer(mTracker, bytes) == FSDK.FSDKE_OK) {
                String TemplateString = Base64.encodeToString(bytes, Base64.DEFAULT);
                Log.i(TAG, "saveTracker: LOOOOOOOOOOOOOOOOOOORD " + TemplateString );
                mFace.setAuth(1);
                mFace.setTemplate(TemplateString);
                saveImageToDir();
                event.resetParamsNewLoad();
            }
            else
            {
                Log.i(TAG, "saveTracker: COULDN'T SAVE THE TRACKER");
                //TODO IMPLEMENT ERROR CALL BACK

            }
        }

        else if ( mFace.getTemplate().length() > 10 && mFace.getSingleTemplate().length() < 10){
            Log.i(TAG, "SAVE SINGLE TRACKER");
            if (FSDK.SaveTrackerMemoryToBuffer(mTracker, bytes) == FSDK.FSDKE_OK) {
                String TemplateString = Base64.encodeToString(bytes, Base64.DEFAULT);
                mFace.setAuth(1);
                mFace.setSingleTemplate(TemplateString);
                new SaveFace().execute(mFace);
                CustomLuxandActivity.BGFR_FLOW =  BGFRFlow.SINGLE_CAPTURE;
                event.TrackerSavedFirst();

            }
            else
            {
                Log.i(TAG, "saveTracker: COULDN'T SAVE THE TRACKER");
                //TODO IMPLEMENT ERROR CALL BACK

            }
        }


    }


    /**
     * Saves the Current HImage to a file, using the FSDK Implementation
     */
    public void savePicture(){
        if(this.mYUVData == null)
            Log.i(TAG, "savePicture: YUV IS NULL");
        else
        {
            try{
                FileOutputStream stream     =  new FileOutputStream(mContext.getExternalFilesDir("")+ "/capture.jpg");
                YuvImage yuvImage           = new YuvImage(this.mYUVData, ImageFormat.NV21, mImageWidth, mImageHeight, null);
                yuvImage.compressToJpeg(new Rect(0, 0, mImageWidth, mImageHeight), 30, stream);
            }catch (FileNotFoundException f){

            }
        }

    }

    public void saveImageToDirOnce(){
        Runnable saveRunnable  =  ()->{
            HImage Image                                                    = new HImage();
            imagemode                                                       = new FSDK_IMAGEMODE();
            imagemode.mode                                                  = FSDK_IMAGEMODE.FSDK_IMAGE_COLOR_24BIT;
            FSDK.LoadImageFromBuffer(Image, mRGBData, mImageWidth, mImageHeight, mImageWidth * 3, imagemode);
            if (this.cameraType == 1) {
                FSDK.MirrorImage(Image, false);
            }
            HImage RotatedImage                                                    = new HImage();
            FSDK.CreateEmptyImage(RotatedImage);

            /**it is necessary to work with local variables
             *  (onDraw called not the time when mImageWidth,... being reassigned,
             *  so swapping mImageWidth and mImageHeight may be not safe)
             */

            if (rotated)
            {
                if (this.cameraType == 1) {
                    FSDK.RotateImage90(Image, -1, RotatedImage);
                } else {
                    FSDK.RotateImage90(Image, 1, RotatedImage);
                }
            }
            else
            {
                FSDK.CopyImage(Image, RotatedImage);
            }
            FSDK.SetJpegCompressionQuality(1);
            File imagefile =  new File( mContext.getExternalFilesDir("")+ "/capture.jpg");
            Log.i(TAG, "savePicture: ABOUT TO SAVE TO ONE TIME IMAGE " + imagefile.getAbsolutePath());
            if(FSDK.FSDKE_OK == FSDK.SaveImageToFile(RotatedImage,imagefile.getAbsolutePath())) {
                Log.i(TAG, "saveImageToDir: THE FILE WAS SAVED SUCCESSFULLY TO THE DATA DIRECTORY @ " + imagefile.getAbsolutePath());
            }

        };
        try {
            if(!SAVED_IMAGE)
            {
                SAVED_IMAGE=true;
                saveRunnable.run();
            }
        } catch(Exception e){
            Log.i(TAG, "saveImageToDir: EXCEPTION CAUGHT IN SAVE RUNNNABLE ASYNC TASK ONE TIME IMAGE :  " + e.getMessage());
        }


    }
    public void saveImageToDir(){
       Runnable saveRunnable  =  ()->{
                HImage Image                                                    = new HImage();
                imagemode                                                       = new FSDK_IMAGEMODE();
                imagemode.mode                                                  = FSDK_IMAGEMODE.FSDK_IMAGE_COLOR_24BIT;
                FSDK.LoadImageFromBuffer(Image, mRGBData, mImageWidth, mImageHeight, mImageWidth * 3, imagemode);
           if (this.cameraType == 1) {
               FSDK.MirrorImage(Image, false);
           }
           HImage RotatedImage                                                    = new HImage();
           FSDK.CreateEmptyImage(RotatedImage);

           /**it is necessary to work with local variables
            *  (onDraw called not the time when mImageWidth,... being reassigned,
            *  so swapping mImageWidth and mImageHeight may be not safe)
            */

           if (rotated)
           {
               if (this.cameraType == 1) {
                   FSDK.RotateImage90(Image, -1, RotatedImage);
               } else {
                   FSDK.RotateImage90(Image, 1, RotatedImage);
               }
           }
           else
           {
               FSDK.CopyImage(Image, RotatedImage);
           }
                FSDK.SetJpegCompressionQuality(1);
                File imagefile =  new File( mContext.getExternalFilesDir("")+ "/capture.jpg");
                Log.i(TAG, "savePicture: ABOUT TO SAVE TO " + imagefile.getAbsolutePath());
                if(FSDK.FSDKE_OK == FSDK.SaveImageToFile(RotatedImage,imagefile.getAbsolutePath())) {
                    Log.i(TAG, "saveImageToDir: THE FILE WAS SAVED SUCCESSFULLY TO THE DATA DIRECTORY @ " + imagefile.getAbsolutePath());
                }

            };
        try {
            saveRunnable.run();
        } catch(Exception e){
            Log.i(TAG, "saveImageToDir: EXCEPTION CAUGHT IN SAVE RUNNNABLE ASYNC TASK :  " + e.getMessage());
        }


    }

    /**
     * Get the range of values from the Expression Detection, and returns tru if greater than the prefixed threshold
     * of "40"
     */
    public boolean isRealFace(){

        if(!detectFakeFaces)
            return true;

        int range = 0;

        Collections.sort(DetectedExpressions);
        Log.i(TAG, "isRealFace: CALLED IS REAL FACE " + DetectedExpressions.get(DetectedExpressions.size()-1) + " " + DetectedExpressions.get(0));
        if(DetectedExpressions.size() > 1)
            range = DetectedExpressions.get(DetectedExpressions.size()-1) - DetectedExpressions.get(0);
        if(range > 22) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isDetectFakeFaces() {
        return this.detectFakeFaces;
    }

    public void setDetectFakeFaces(boolean detectFakeFaces) {
        this.detectFakeFaces = detectFakeFaces;
        Log.i(TAG, "setDetectFakeFaces: THIS CURRENT SESSION WAS SET FAKE FACES TO " + detectFakeFaces);
    }

    public BGFRMode getMode() {
        return mode;
    }

    public void setMode(BGFRMode mode) {
        Log.i(TAG, "setMode: THE MODE OF THE CURRENT SESSION WAS SET TO " +mode);
        this.mode = mode;
    }



    /**
     * AsyncTask to save template and image to the database
     */
    private class SaveFace extends AsyncTask<Face,Void,Void> {
        @Override
        protected Void doInBackground(Face...faces) {
            CustomLuxandActivity.repository.insertFace(faces[0]);
            Log.i(TAG, "doInBackground: THE SIZES OF THE STORED FACES ARE PILE-OF-TRACKERS = " + faces[0].getTemplate().length()
                    + " ||  SINGLE-TRACKER = " + faces[0].getSingleTemplate().length() );
            return null;
        }
    }

    /**
     *
     * AsyncTask to save template and image to the database
     */
    private class SaveUnbundledFace extends AsyncTask<Face,Void,Void> {
        @Override
        protected Void doInBackground(Face...faces) {

            CustomLuxandActivity.repository.faceDAO.updateLastFaceImage(feature_name,faces[0].getTemplate());


            return null;
        }
    }



}






