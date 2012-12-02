package nl.napauleon.sabber.http;

import nl.napauleon.sabber.ContextHelper;
import android.content.Context;

public abstract class DefaultErrorCallback implements HttpCallback{

	public void handleError(Context context, String error) {
		new ContextHelper(context).showErrorAlert(error);
	}

	public void handleTimeout(Context context) {
		new ContextHelper(context).showConnectionTimeoutAlert();
	}
}
