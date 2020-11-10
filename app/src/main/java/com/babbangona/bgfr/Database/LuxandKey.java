package com.babbangona.bgfr.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "LUXAND_KEY")
public class LuxandKey {
    /**
     * TABLE DEFINITION
     * ==================
     * || LUXAND_KEY (PPY. KEY) ||
     * ||-----------------------||
     * ||                       ||
     */
    @PrimaryKey
    @NonNull
    @ColumnInfo(name                = "LUXAND_KEY")

    public String Key;
    @NonNull
    public String getKey() {
        return Key;
    }

    public void setKey(@NonNull String key) {
        Key = key;
    }

    public LuxandKey(String key){
        this.Key = key;
    }

    public LuxandKey setPredefinedKey(){
        return new LuxandKey("hoynDpHEai2hNQwtcWI8fVFuDotDyliNysu37ydK1Dd9iV7wNfpJizHURKq/q+LAkdv9zqYS48mD5+RmmPNX3njok2bW712DubyBU4lSP4twqFuVvnguUGbZwqjCNQlVg8S5FAXldxXhozC7LDahJBQnFxYo5MPwUcZwY9OcRvY=");

    }

    public LuxandKey(){

    }
}
