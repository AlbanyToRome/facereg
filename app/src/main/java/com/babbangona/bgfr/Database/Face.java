package com.babbangona.bgfr.Database;

import android.security.keystore.UserNotAuthenticatedException;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FACE_DESCRIPTION")
public class Face {

    /**
     * TABLE DEFINITION
     * =================
     * ~ TEMPLATE   - TRACKER STRING AS GOTTEN FROM LUXAND
     * ~ NAME       - STATIC NAME DEFAULTED TO "BG"
     * ~ AUTH       - AUTHENTICATION FLAG. SET TO 1 WHEN IN CAPTURE MODE
     * =====================================================================
     * || TEMPLATE  || NAME (PPY. KEY) || IMAGE (DEFAULT TO "0") ||AUTH (DEFAULT 1 IN CAPTURE MODE) || Single TEMPLATE       ||
     * ||-----------||-----------------||------------------------||---------------------------------||--------------------------||
     * ||           ||                 ||                        ||                                 ||                          ||
     */


    @NonNull
    @ColumnInfo(name                = "TRACKER")
    public String Template          = "0";

    @NonNull
    @ColumnInfo(name                = "SINGLE_TRACKER")
    public String SingleTemplate = "0";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name                = "NAME")
    public String Name              = "BG";

    @NonNull
    @ColumnInfo(name                = "IMAGE")
    public String Image             = "0";

    @NonNull
    @ColumnInfo(name                = "AUTH_FLAG")
    public int Auth                 = 0;

    @NonNull
    public String getTemplate() {
        return Template;
    }

    public void setTemplate(@NonNull String template) {
        Template                    = template;
    }

    @NonNull
    public String getName() {
        return Name;
    }

    public void setName(@NonNull String name) {
        Name                        = name;
    }

    public int getAuth() {
        return Auth;
    }

    public void setAuth(int auth) {
        Auth                        = auth;
    }

    public void setSingleTemplate(@NonNull String template){
        SingleTemplate                                       = template;
    }

    public String getSingleTemplate(){
        return SingleTemplate;
    }

    @NonNull
    public String getImage() {
        return Image;
    }

    public void setImage(@NonNull String image) {
        Image = image;
    }
}
