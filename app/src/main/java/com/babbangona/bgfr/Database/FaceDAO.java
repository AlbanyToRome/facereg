package com.babbangona.bgfr.Database;

import android.database.Cursor;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
@Dao
public interface FaceDAO {

    /**
     * TABLE DEFINITION
     * =================
     * || TEMPLATE  || NAME (PPY. KEY) || AUTH (DEFAULT 1 IN CAPTURE MODE) ||
     * ||-----------||-----------------||----------------------------------||
     * ||           ||                 ||                                  ||
     */

    @Insert
    void insert(Face face);

    @Query("DELETE FROM FACE_DESCRIPTION")
    void deleteAll();

    @Query("SELECT * from FACE_DESCRIPTION ORDER BY NAME DESC")
    List<Face> getAllFace();

    @Query("SELECT * from FACE_DESCRIPTION ORDER BY NAME DESC LIMIT 1")
    Cursor getLastFace();

    @Query("UPDATE FACE_DESCRIPTION SET SINGLE_TRACKER = :Image WHERE NAME  = :id " )
    void updateLastFaceImage(String id, String Image);

    @Update
    int updateFace(Face face);
}
