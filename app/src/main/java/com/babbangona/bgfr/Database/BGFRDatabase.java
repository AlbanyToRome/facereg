package com.babbangona.bgfr.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


/**
 * DATABASE DEFINITION
 * =================
 * ~ FACE TABLE                 - TABLE FOR FACE DESCRIPTION CONTAINING TRACKER ET AL
 * ~ LUXAND KEY TABLE           - TABLE HOLDING CURRENT LUXAND AUTH KEY
 *
 * ||       TEMPLATE            ||
 * ||---------------------------||
 * || *** FACE_TABLE            ||
 * || *** LUXAND KEY TABLE      ||
 */
@Database(entities = {Face.class,LuxandKey.class}, version = 3, exportSchema = false)
public abstract class BGFRDatabase extends RoomDatabase {

    //Data Access Objects for the two tables defined above
    public abstract FaceDAO         faceDAO();
    public abstract LuxandKeyDAO   luxandKeyDAO();

    private static volatile BGFRDatabase INSTANCE;
    static BGFRDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BGFRDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BGFRDatabase.class, "BGFR_Database.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
