package nl.napauleon.sabber.http;

import java.io.IOException;

import nl.napauleon.sabber.Constants;
import nl.napauleon.sabber.Sabber;
import android.util.Log;

public class HttpGetMockTask extends GetTask {

    private static final String TAG = Constants.TAG + ".HttpGetTask";

    public HttpGetMockTask(HttpCallback callback) {
		super(callback);
	}

	@Override
    protected String doInBackground(String... strings) {
       try {
		return inputStreamToString(Sabber.getContext().getAssets().open(strings[0])).toString();
	} catch (IOException e) {
		Log.e(TAG, "error reading file for "+ strings[0]);
	}
       return null;
    }
}
