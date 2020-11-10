package com.babbangona.bgfr.Database;

import android.app.Application;
import android.os.AsyncTask;

import java.util.Calendar;
import java.util.List;

public class MainRepository {
    /**
     * The repository is similar to the database helper class
     * used in sqlite.
     * It utilizes the Data Access objects to manipulate the database
     */

    public FaceDAO       faceDAO;
    private LuxandKeyDAO keyDAO;

    /**
     * The DAOs are initialized for use in the AsyncTasks
     * @param application, source from which the context is obtained
     *
     */
   public  MainRepository (Application application) {
        BGFRDatabase db             = BGFRDatabase.getDatabase(application);
        faceDAO                     = db.faceDAO();
        keyDAO                      = db.luxandKeyDAO();
    }

    /**
     * Insert Face function which calls it's accompanying ayncTask
     * @param face - Face Description entity which is to be inserted into the table
     */
    public void insertFace (Face face) {
        face.setName("ID" + Calendar.getInstance().getTime());
        faceDAO.deleteAll();
        faceDAO.insert(face);
    }

    public void insertFace (String Template) {
        Face face = new Face();
        face.setName("ID" + Calendar.getInstance().getTime());
        face.setTemplate(Template);
        faceDAO.deleteAll();
        faceDAO.insert(face);
    }

    /**
     * Insert Key function which calls it's accompanying ayncTask
     * @param key, key which is to updated in the key table
     */
    public void insertKey(LuxandKey key)
    {
        keyDAO.deleteAll();
        keyDAO.insert(key);
    }

    /**
     * Get the most recent face from the table
     * fool proof just in case developer (me) uses an insert instead of an update
     */
    public Face getFace(){
        List<Face> faces =  faceDAO.getAllFace();
        if(faces != null && faces.size() > 0)
            return faces.get(0);
        else
            return new Face();

    }



}
