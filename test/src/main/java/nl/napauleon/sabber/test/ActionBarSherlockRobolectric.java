package nl.napauleon.sabber.test;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.internal.ActionBarSherlockNative;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@ActionBarSherlock.Implementation(api = 0)
public class ActionBarSherlockRobolectric extends ActionBarSherlockNative {
    public ActionBarSherlockRobolectric(Activity activity, int flags) {
        super(activity, flags);
    }

    @Override
    public void setContentView(int layoutResId) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        View view = layoutInflater.inflate(layoutResId, null);
        shadowOf(mActivity).setContentView(view);

    }

    @Override
    public void setContentView(View view) {
        shadowOf(mActivity).setContentView(view);
    }
}
