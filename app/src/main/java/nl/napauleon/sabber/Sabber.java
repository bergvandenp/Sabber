package nl.napauleon.sabber;

import android.app.Application;
import android.content.Context;

public class Sabber extends Application{
	
	private static Sabber instance;

    public Sabber() {
    	instance = this;
    }

    public static Context getContext() {
    	return instance;
    }

}
