package nl.napauleon.sabber.test;

import com.jayway.android.robotium.solo.Solo;

import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.R;
import android.test.ActivityInstrumentationTestCase2;

public abstract class RobotiumTest extends ActivityInstrumentationTestCase2<MainActivity> {

	protected Solo solo;

	public RobotiumTest() {
		super(MainActivity.class);
	}
	
	@Override
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	protected void enterMockSettings() {
		solo.sendKey(Solo.MENU);
		solo.clickOnActionBarItem(R.id.menu_settings);
		
		solo.clickOnText(solo.getString(R.string.hostname));
		solo.clearEditText(0);
		solo.enterText(0, "123");
		solo.clickOnButton("OK");
		
		solo.clickOnText(solo.getString(R.string.port));
		solo.clearEditText(0);
		solo.enterText(0, "666");
		solo.clickOnButton("OK");
		
		solo.clickOnText(solo.getString(R.string.apikey));
		solo.clearEditText(0);
		solo.enterText(0, "123");
		solo.clickOnButton("OK");
		
		solo.goBack();
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}
}
