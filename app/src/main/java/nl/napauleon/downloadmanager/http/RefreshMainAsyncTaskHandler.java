package nl.napauleon.downloadmanager.http;

import android.content.Context;
import android.content.Intent;
import nl.napauleon.downloadmanager.MainActivity;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: napauleon
 * Date: 5/5/12
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class RefreshMainAsyncTaskHandler implements AsyncTaskHandler{

    private final Context context;

    public RefreshMainAsyncTaskHandler(Context context) {
        this.context = context;
    }

    public void handleResult(String result) throws JSONException, UnsupportedEncodingException {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
}
