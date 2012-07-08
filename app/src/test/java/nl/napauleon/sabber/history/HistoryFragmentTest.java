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
        assertEquals("", fragment.getHistoryItems().get(0).getMessage());
    }

    @Test
    public void testHandleHistoryResult_repair() throws Exception {
        Message message = Utils.createResultMessage("historyresult_repair.json");

        fragment.new HistoryHandler().handleMessage(message);

        assertEquals(1, fragment.getHistoryItems().size());
        HistoryInfo historyInfo = fragment.getHistoryItems().get(0);
        assertEquals(Status.Repairing, historyInfo.getStatus());
        assertEquals("Repairing: 85%", historyInfo.getMessage());

    }

    @Test
    public void testHandleHistoryResult_extract() throws Exception {
        Message message = Utils.createResultMessage("historyresult_extract.json");

        fragment.new HistoryHandler().handleMessage(message);

        assertEquals(1, fragment.getHistoryItems().size());
        HistoryInfo historyInfo = fragment.getHistoryItems().get(0);
        assertEquals(Status.Extracting, historyInfo.getStatus());
        assertEquals("Unpacking: 25/30", historyInfo.getMessage());
    }

    @Test
    public void testHandleHistoryResult_verify() throws Exception {
        Message message = Utils.createResultMessage("historyresult_verify.json");

        fragment.new HistoryHandler().handleMessage(message);

        assertEquals(1, fragment.getHistoryItems().size());
        HistoryInfo historyInfo = fragment.getHistoryItems().get(0);
        assertEquals(Status.Verifying, historyInfo.getStatus());
        assertEquals("Verifying: 16/30", historyInfo.getMessage());
    }

    @Test
    public void testHandleHistoryResult_failed() throws Exception {
        Message message = Utils.createResultMessage("historyresult_failed.json");

        fragment.new HistoryHandler().handleMessage(message);

        assertEquals(3, fragment.getHistoryItems().size());
        HistoryInfo historyInfo = fragment.getHistoryItems().get(2);
        assertEquals(Status.Failed, historyInfo.getStatus());
        assertEquals("Download failed - Out of your server's retention?", historyInfo.getMessage());
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
