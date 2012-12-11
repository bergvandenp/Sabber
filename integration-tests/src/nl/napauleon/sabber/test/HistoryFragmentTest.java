package nl.napauleon.sabber.test;

import nl.napauleon.sabber.R;

public class HistoryFragmentTest extends RobotiumTest {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enterMockSettings();
	}

	public void testHistoryMock() throws Exception {
		
		enterMockSettings();
		
		solo.clickOnText(solo.getString(R.string.history));
		assertTrue(solo.searchText("Anne Rice"));
	}
}
