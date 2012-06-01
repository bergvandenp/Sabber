package nl.napauleon.sabber.history;

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

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class HistoryFragmentTest {

    HistoryFragment fragment;

    @Before
    public void setUp() throws Exception {
        fragment = new HistoryFragment();
        fragment.onCreate(null);
    }

    @Test
    public void testHandleHistoryResult() throws Exception {
        Message message = Utils.createResultMessage("historyresult");

        fragment.new HistoryHandler().handleMessage(message);

        assertEquals(7, fragment.getHistoryItems().size());
    }

    @Test
    public void testCreateHistoryConnection() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(new Activity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ContextHelper.HOSTNAME_PREF, "localhost");
        editor.putString(ContextHelper.PORT_PREF, "8080");
        editor.putString(ContextHelper.APIKEY_PREF, "abc");
        editor.commit();

        String expected = "http://localhost:8080/api?mode=history&limit=100&output=json&apikey=abc";
        String result = fragment.createHistoryConnectionString(preferences);

        assertEquals(expected, result);
    }
}
