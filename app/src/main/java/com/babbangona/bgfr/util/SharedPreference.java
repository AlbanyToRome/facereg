package com.babbangona.faceapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.util.Pair;

import com.google.gson.Gson;

public class SharedPreference {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    //CONSTANTS
    public static final int PUT_SUCCESS     = 0;
    public static final int PUT_FAIL        = -1;

    public SharedPreference(Context context) {
        this.preferences        = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        this.editor             = this.preferences.edit();
        this.gson               = new Gson();


    }

    /**
     * GENERIC FUNCTION TO STORE ANYTHING TO SHARED PREFS AS LONG AS IT IS SERIALIZABLE BY THE GSON CONVERTER
     * @param key -  THE STRING KEY VALUE OF THE OBJECT TO BE STORED
     * @param value - THE VALUE TO BE STORED IN THE SHARED PREFS
     * @param <T> - THE TYPE BEING STORED
     * @return- RESULT, 0- SUCCESSFUL, 1-UNSUCCESSFUL
     */
    public <T> int  putValue(String key, T value){

        if      (value instanceof String)
            editor.putString(key,(String)value);

        else if (value instanceof Integer)
            editor.putInt(key,(Integer)value);

        else if (value instanceof Float)
            editor.putFloat(key,(Float)value);

        else{
            try{
                String val = gson.toJson(value);
                editor.putString(key, val);
            } catch (Exception e){
                return PUT_FAIL;
            }
        }

        editor.commit();
        return  PUT_SUCCESS;
    }

    public <T> int  putValues(Pair<String, T>... keyValuePairs){
        int res = 0;
        for (Pair<String, T> keyValue : keyValuePairs)
            res +=  putValue(keyValue.first,keyValue.second);
        return res < 0 ? PUT_FAIL : PUT_SUCCESS;
    }

    public <T> T getValue(Class<T> value, String key){
        if      (value == String.class)
            return (T)preferences.getString(key, "");

        else if (value == Integer.class)
            return (T)(Integer)preferences.getInt(key, 0);


        else if (value == Float.class)
            return (T)(Float)preferences.getFloat(key, 0);

        else{
            try{
                String val = preferences.getString(key,"");
                if(val.isEmpty())
                    return null;
                else
                {
                    return gson.fromJson(val,value);
                }
            } catch (Exception e){
                //Log.i(Constants.Logger.TAG, "getValue: Exception occurred in POJO Xform " + e.getMessage());
                return null;
            }
        }

    }

    public <T> T getValue(Class<T> value, String key, T defaultValue){
        if      (value == String.class)
            return (T)preferences.getString(key, (String)defaultValue);

        else if (value == Integer.class)
            return (T)(Integer)preferences.getInt(key, (Integer) defaultValue);


        else if (value == Float.class)
            return (T)(Float)preferences.getFloat(key, (Float) defaultValue);

        else{
            try{
                String val = preferences.getString(key,"");
                if(val.isEmpty())
                    return null;
                else
                {
                    return gson.fromJson(val,value);
                }
            } catch (Exception e){
                //Log.i(Constants.Logger.TAG, "getValue: Exception occurred in POJO Xform " + e.getMessage());
                return null;
            }
        }

    }
}
