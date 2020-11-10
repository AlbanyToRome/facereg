package com.babbangona.bgfr;

/**
 * This Callback function is used an event notifier for the main UI thread
 * using the Luxand Activity, once capture or authentication is completed, then the
 * activity should clean up and caller should proceed as expected
 */
public interface BGCallback {
    public void Authenticated();

    public void FaceFound();

    public void resetParamsNewLoad();

    public void TrackerSavedFirst();

}
