package nl.napauleon.sabber.queue;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class DownloadingFragmentTest {

    DownloadingFragment fragment;

    @Before
    public void setUp() throws Exception {
        fragment = new DownloadingFragment();
        fragment.onCreate(null);
    }

    @Test
    public void testHandleQueueResult() throws Exception {
        Message message = Utils.createResultMessage("queueresult");

//        fragment.new DownloadingCallback().handleMessage(message);

        List<QueueInfo> result = fragment.getQueueItems();
        assertEquals(1, result.size());
        assertEquals("Rookie.Blue.S03E02.720p.HDTV.x264-IMMERSE", result.get(0).getItem());
    }

    @Test
    public void testCreateQueueConnection() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(new Activity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ContextHelper.HOSTNAME_PREF, "localhost");
        editor.putString(ContextHelper.PORT_PREF, "8080");
        editor.putString(ContextHelper.APIKEY_PREF, "abc");
        editor.commit();

        String expected = "http://localhost:8080/api?mode=queue&output=json&apikey=abc";
        String result = fragment.createQueueConnectionString(preferences);

        assertEquals(expected, result);
    }
}
