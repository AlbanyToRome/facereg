package com.babbangona.bgfr.Database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
@Dao
public interface LuxandKeyDAO {
    /**
     * TABLE DEFINITION
     * ==================
     * || LUXAND_KEY (PPY. KEY) ||
     * ||-----------------------||
     * ||                       ||
     */
    @Insert
    void insert(LuxandKey key);

    @Query("DELETE FROM LUXAND_KEY")
    void deleteAll();

    @Query("SELECT * from LUXAND_KEY")
    List<LuxandKey> getAllKeys();

    @Update
    int updateKey(LuxandKey key);
}
