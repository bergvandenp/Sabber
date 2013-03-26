package nl.napauleon.sabber.history;

import android.content.SharedPreferences;
import com.xtremelabs.robolectric.shadows.ShadowPreferenceManager;
import nl.napauleon.sabber.Constants;
import nl.napauleon.sabber.CustomTestRunner;
import nl.napauleon.sabber.MainActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(CustomTestRunner.class)
public class NotificationServiceTest {
	
	NotificationService service;
    private Calendar downloadDate;

    @Before
	public void setUp() {
        SharedPreferences preferences = ShadowPreferenceManager.getDefaultSharedPreferences(new MainActivity());
        SharedPreferences.Editor editor = preferences.edit();
        Calendar lastPollingDate = Calendar.getInstance();
        lastPollingDate.add(Calendar.MINUTE, -2);
        editor.putLong(Constants.LAST_POLLING_EVENT_PREF, lastPollingDate.getTimeInMillis());
        editor.commit();
		service = new NotificationService();

        downloadDate = Calendar.getInstance();
        downloadDate.add(Calendar.MINUTE, -1);
	}

    @Test
    public void testShouldNotify_Completed() {
        boolean result = service.shouldNotify(new HistoryInfo("bla", downloadDate.getTimeInMillis(), Status.Completed, "bla"));
        assertTrue(result);
    }

    @Test
    public void testShouldNotify_Failed() {
        boolean result = service.shouldNotify(new HistoryInfo("bla", downloadDate.getTimeInMillis(), Status.Failed, "bla"));
        assertTrue(result);
    }

    @Test
    public void testShouldNotify_False_Extracting() {
        boolean result = service.shouldNotify(new HistoryInfo("bla", downloadDate.getTimeInMillis(), Status.Extracting, "bla"));
        assertFalse(result);
    }

    @Test
    public void testShouldNotify_False_AlreadyNotified() {
        Calendar dateDownloaded = Calendar.getInstance();
        dateDownloaded.add(Calendar.MINUTE, -4);
        boolean result = service.shouldNotify(new HistoryInfo("bla", dateDownloaded.getTimeInMillis(), Status.Completed, "bla"));
        assertFalse(result);
    }

    @Test
    public void testGetNotificationContent_Completed() {
        String itemnaam = "Bla die bla";
        HistoryInfo item = new HistoryInfo(itemnaam, Calendar.getInstance().getTimeInMillis(), Status.Completed, "bla");
        String expected = itemnaam + " download completed.";

        String result = service.getNotificationContent(item);

        assertEquals(expected, result);
    }

    @Test
    public void testGetNotificationContent_Failed() {
        String itemnaam = "Bla die bla";
        HistoryInfo item = new HistoryInfo(itemnaam, Calendar.getInstance().getTimeInMillis(), Status.Failed, "bla");
        String expected = itemnaam + " download failed.";

        String result = service.getNotificationContent(item);

        assertEquals(expected, result);
    }

    @Test
    public void testGetNotificationTitle_Completed() {
        HistoryInfo item = new HistoryInfo("bla", Calendar.getInstance().getTimeInMillis(), Status.Completed, "bla");
        String expected = "NZB download completed";

        String result = service.getNotificationTitle(item);

        assertEquals(expected, result);
    }

    @Test
    public void testGetNotificationTitle_Failed() {
        HistoryInfo item = new HistoryInfo("bla", Calendar.getInstance().getTimeInMillis(), Status.Failed, "bla");
        String expected = "NZB download failed";

        String result = service.getNotificationTitle(item);

        assertEquals(expected, result);
    }
	
}
