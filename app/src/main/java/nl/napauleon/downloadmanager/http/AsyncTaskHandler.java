package nl.napauleon.downloadmanager.http;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: napauleon
 * Date: 5/5/12
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AsyncTaskHandler {
    void handleResult(String result) throws JSONException, UnsupportedEncodingException;
}
