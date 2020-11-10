package com.babbangona.bgfr.Database;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.babbangona.bgfr.BGFRActivity;
import com.babbangona.bgfr.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static com.babbangona.bgfr.BGFRActivity.TAG;


public class BGFRInfo {
    private Context mContext;
    private static MainRepository repository;
    public BGFRInfo(Context context){
        mContext                                = context;
        repository                              = new MainRepository(((Activity)context).getApplication());
    }
    public void putTemplate(String Template){
        new putFace().execute(Template);

    }

    public String getTemplate(){
        try {
            return new getFace().execute(0).get();
        }
        catch(Exception e){
            return  "ASYNC TASK ERROR, COULDN'T GET TEMPLATE FROM DATABASE";
        }
}

    public String getBundledTemplate(){
        try {
            return new getFace().execute(0).get();
        }
        catch(Exception e){
            return  "ASYNC TASK ERROR, COULDN'T GET TEMPLATE FROM DATABASE";
        }
    }

    public String getSingleTemplate(){
        try {
            return new getFace().execute(1).get();
        }
        catch(Exception e){
            return  "ASYNC TASK ERROR, COULDN'T GET TEMPLATE FROM DATABASE";
        }
    }

    public Bitmap getImage(){
        try {
            File imagefile =  new File( mContext.getExternalFilesDir("")+ "/capture.jpg");
            File imagefile2 =  new File( mContext.getExternalFilesDir("")+ "/capture2.jpg");
            if(imagefile.exists())
            {
                Bitmap rgbFrameBitmap =  BitmapFactory.decodeFile(imagefile.getAbsolutePath());
                int ratio =  rgbFrameBitmap.getHeight() /rgbFrameBitmap.getWidth();
                Bitmap croppedBitmap = Bitmap.createBitmap(300 , 300, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(croppedBitmap);
                Log.i(TAG, "getImage: WIDTH = " + rgbFrameBitmap.getWidth() + " HEIGHT = " + rgbFrameBitmap.getHeight());
                Matrix frameToCropTransform =
                        ImageUtils.getTransformationMatrix(
                                rgbFrameBitmap.getWidth(), rgbFrameBitmap.getHeight(),
                                224,224,
                                0,true);
                canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG,40,new FileOutputStream(imagefile2));
                return croppedBitmap;

            }
            else
                return null;
        }
        catch(Exception e){
            return null;
        }

    }


    public String getImageString(){
        try {
            return new getThumbNailString().execute(0).get();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public String getBiggerImageString(){
        try {
            return new getThumbNailString().execute(1).get();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public Bitmap getRawBitmap(){
        return BitmapFactory.decodeFile(mContext.getExternalFilesDir("")+ "/capture.jpg");
    }

    /**
     * AsyncTask to save template and image to the database
     */
    private class putFace extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String...faces) {
            repository.insertFace(faces[0]);
            return null;
        }
    }

    private class getFace extends AsyncTask<Integer,String,String> {
        @Override
        protected String doInBackground(Integer...faces) {
            Face face = new Face();
            String res = "";
            switch(faces[0]){

                case 0: face = repository.getFace();
                    res =  face.getTemplate();
                break;
                case 1:
                    face = repository.getFace();
                    res =  face.getSingleTemplate();
                break;

            }
            return res;
        }
    }

    private class getImage extends AsyncTask<Integer,String,String> {
        @Override
        protected String doInBackground(Integer...faces) {

            Face face = repository.getFace();
            return face.getImage();
        }
    }

    private class getThumbNailString extends AsyncTask<Integer,String,String>{
        @Override
        protected String doInBackground(Integer...faces) {
            try {

                File imagefile =  new File( mContext.getExternalFilesDir("")+ "/capture.jpg");
                ByteArrayOutputStream imagefile2 =  new ByteArrayOutputStream();
                if(imagefile.exists())
                {
                    Bitmap rgbFrameBitmap =  BitmapFactory.decodeFile(imagefile.getAbsolutePath());
                    int ratio =  rgbFrameBitmap.getHeight() /rgbFrameBitmap.getWidth();
                    if(faces[0] == 0) {
                        Bitmap croppedBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
                        final Canvas canvas = new Canvas(croppedBitmap);
                        Log.i(TAG, "getImage: WIDTH = " + rgbFrameBitmap.getWidth() + " HEIGHT = " + rgbFrameBitmap.getHeight());
                        Matrix frameToCropTransform =
                                ImageUtils.getTransformationMatrix(
                                        rgbFrameBitmap.getWidth(), rgbFrameBitmap.getHeight(),
                                        224, 224,
                                        0, true);
                        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, imagefile2);
                        byte[] bytes = imagefile2.toByteArray();
                        return Base64.encodeToString(bytes, Base64.DEFAULT);
                    }
                    else
                    {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        rgbFrameBitmap.compress(Bitmap.CompressFormat.JPEG,30,stream);
                        byte[] bytes = stream.toByteArray();
                        Log.i(TAG, "doInBackground: SIZE OF THE 10% QUALITY COMPRESSION =  " +  bytes.length);
                        return Base64.encodeToString(bytes, Base64.DEFAULT);


                    }

                }
                else
                    return null;
            }
            catch(Exception e){
                return null;
            }
        }
    }
}
