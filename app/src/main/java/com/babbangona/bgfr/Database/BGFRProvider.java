package com.babbangona.bgfr.Database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;

public class BGFRProvider extends ContentProvider {

    /**
     * Static variables needed in the creation of the class
     *
     */
    static final String PROVIDER_NAME           = "com.babbangona.bgfr.Database.BGFRProvider";
    static final String URL                     = "content://" + PROVIDER_NAME + "/" + "FACE_DESCRIPTION";
    static final Uri CONTENT_URI                = Uri.parse(URL);

    static final int uriCode                    = 1;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;
    static
    {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "users", uriCode);
        uriMatcher.addURI(PROVIDER_NAME, "users/*", uriCode);
    }

    // Defines a handle to the Room database
    private BGFRDatabase database;

    // Defines a Data Access Object to perform the database operations
    private FaceDAO faceDAO;

    // Defines the database name
    private static final String DBNAME = "BGFR_Database";

    public boolean onCreate() {

        // Creates a new database object.
        database = BGFRDatabase.getDatabase(getContext());

        // Gets a Data Access Object to perform the database operations
        faceDAO = database.faceDAO();

        return true;
    }

    /**
     *
     * @param uri URI FROM THE CALLING ACTIVITY OR APPLICATION
     * @return
     */
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case uriCode:
                return URL;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * NB: QUERIES WOULD BE DISALLOWED IN THE CONTENT PROVIDER
     * JUST FOR SETTING AND GETTING TEMPLATE
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

            if(selection == null) return  faceDAO.getLastFace();
            else {
                Face face =  new Face();
                face.setName("BG");
                face.setTemplate(selection);
                faceDAO.insert(face);
                return  null;
            }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
      return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }




}
