package nl.napauleon.sabber.shadow;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.SpinnerAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowActivity;

import java.io.FileDescriptor;
import java.io.PrintWriter;

@Implements(ShadowActivity.class)
public class MyShadowFragmentActivity extends ShadowActivity {


    @Implementation
    public FragmentManager getSupportFragmentManager() {
        return new FragmentManager() {

            @Override
            public FragmentTransaction beginTransaction() {
                return new FragmentTransaction() {
                    @Override
                    public FragmentTransaction add(Fragment fragment, String s) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction add(int i, Fragment fragment) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction add(int i, Fragment fragment, String s) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction replace(int i, Fragment fragment) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction replace(int i, Fragment fragment, String s) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction remove(Fragment fragment) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction hide(Fragment fragment) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction show(Fragment fragment) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction detach(Fragment fragment) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction attach(Fragment fragment) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction setCustomAnimations(int i, int i1) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction setCustomAnimations(int i, int i1, int i2, int i3) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction setTransition(int i) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction setTransitionStyle(int i) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction addToBackStack(String s) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public boolean isAddToBackStackAllowed() {
                        return false;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction disallowAddToBackStack() {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction setBreadCrumbTitle(int i) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction setBreadCrumbTitle(CharSequence charSequence) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction setBreadCrumbShortTitle(int i) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public FragmentTransaction setBreadCrumbShortTitle(CharSequence charSequence) {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public int commit() {
                        return 0;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public int commitAllowingStateLoss() {
                        return 0;  //To change body of implemented methods use File | Settings | File Templates.
                    }
                };
            }

            @Override
            public boolean executePendingTransactions() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Fragment findFragmentById(int i) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Fragment findFragmentByTag(String s) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void popBackStack() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean popBackStackImmediate() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void popBackStack(String s, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean popBackStackImmediate(String s, int i) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void popBackStack(int i, int i1) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean popBackStackImmediate(int i, int i1) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getBackStackEntryCount() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public BackStackEntry getBackStackEntryAt(int i) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addOnBackStackChangedListener(OnBackStackChangedListener onBackStackChangedListener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeOnBackStackChangedListener(OnBackStackChangedListener onBackStackChangedListener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void putFragment(Bundle bundle, String s, Fragment fragment) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Fragment getFragment(Bundle bundle, String s) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Fragment.SavedState saveFragmentInstanceState(Fragment fragment) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void dump(String s, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strings) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };

    }

    @Implementation
    public ActionBar getSupportActionBar() {
        return new ActionBar() {

            @Override
            public void setCustomView(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCustomView(View view, LayoutParams layoutParams) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCustomView(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setIcon(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setIcon(Drawable drawable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setLogo(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setLogo(Drawable drawable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setListNavigationCallbacks(SpinnerAdapter spinnerAdapter, OnNavigationListener onNavigationListener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setSelectedNavigationItem(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getSelectedNavigationIndex() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getNavigationItemCount() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setTitle(CharSequence charSequence) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setTitle(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setSubtitle(CharSequence charSequence) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setSubtitle(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setDisplayOptions(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setDisplayOptions(int i, int i1) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setDisplayUseLogoEnabled(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setDisplayShowHomeEnabled(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setDisplayHomeAsUpEnabled(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setDisplayShowTitleEnabled(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setDisplayShowCustomEnabled(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setBackgroundDrawable(Drawable drawable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public View getCustomView() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public CharSequence getTitle() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public CharSequence getSubtitle() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getNavigationMode() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setNavigationMode(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getDisplayOptions() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Tab newTab() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addTab(Tab tab) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addTab(Tab tab, boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addTab(Tab tab, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addTab(Tab tab, int i, boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeTab(Tab tab) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeTabAt(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeAllTabs() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void selectTab(Tab tab) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Tab getSelectedTab() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Tab getTabAt(int i) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getTabCount() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getHeight() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void show() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void hide() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isShowing() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addOnMenuVisibilityListener(OnMenuVisibilityListener onMenuVisibilityListener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeOnMenuVisibilityListener(OnMenuVisibilityListener onMenuVisibilityListener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    @Implementation
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
    }
}
