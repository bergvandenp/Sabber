package nl.napauleon.downloadmanager;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class TabListener<T extends Fragment> implements com.actionbarsherlock.app.ActionBar.TabListener {
    private final Activity mActivity;
    private final String mTag;
    private final Class<T> mClass;

    /** Constructor used each time a new tab is created.
      * @param activity  The host Activity, used to instantiate the fragment
      * @param tag  The identifier tag for the fragment
      * @param clz  The fragment's Class, used to instantiate the fragment
      */
    public TabListener(Activity activity, String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
    }

	public void onTabSelected(com.actionbarsherlock.app.ActionBar.Tab tab,
			FragmentTransaction ft) {
		//todo what is ft == null?
		if(ft != null) {
			Fragment mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(android.R.id.content, mFragment, mTag);
		}
		
	}

	public void onTabUnselected(com.actionbarsherlock.app.ActionBar.Tab tab,
			FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	public void onTabReselected(com.actionbarsherlock.app.ActionBar.Tab tab,
			FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
