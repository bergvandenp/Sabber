package nl.napauleon.downloadmanager.http;

import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: napauleon
 * Date: 5/5/12
 * Time: 2:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultAsyncTaskHandler implements AsyncTaskHandler {

    private static final String TAG = "HttpGetTask";

    public void handleResult(String result) {
        Log.i(TAG, "Http result: " + result);
    }
}
