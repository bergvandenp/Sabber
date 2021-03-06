package nl.napauleon.sabber.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.napauleon.sabber.CustomTestRunner;
import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.Utils;
import nl.napauleon.sabber.shadow.MyShadowFragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowToast;

@RunWith(CustomTestRunner.class)
public class HistoryFragmentTest {

    HistoryFragment fragment;

    @Before
    public void setUp() throws Exception {
        fragment = new HistoryFragment();
        Robolectric.shadowOf(fragment).setActivity(new MainActivity());
        startFragment(fragment);
    }

    public static void startFragment( Fragment fragment )
    {
        FragmentManager fragmentManager = new MyShadowFragmentActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, null);
        fragmentTransaction.commit();
    }

    @Test
    public void testRetreiveData() throws Exception {
        fragment.retrieveData();
        assertTrue(ShadowToast.showedToast("Settings are not valid. Check host and port configuration."));
    }

    @Test
    public void testHandleHistoryResult() throws Exception {
        String message = Utils.readFileToString("historyresult");
        
        fragment.new HistoryFragmentCallback().handleResponse(message);

        assertEquals(7, fragment.getHistoryItems().size());
        assertEquals("", fragment.getHistoryItems().get(0).getMessage());
    }

    @Test
    public void testHandleHistoryResult_repair() throws Exception {
        String message = Utils.readFileToString("historyresult_repair.json");

        fragment.new HistoryFragmentCallback().handleResponse(message);
        
        assertEquals(1, fragment.getHistoryItems().size());
        HistoryInfo historyInfo = fragment.getHistoryItems().get(0);
        assertEquals("Repairing: 85%", historyInfo.getMessage());

    }

    @Test
    public void testHandleHistoryResult_extract() throws Exception {
        String message = Utils.readFileToString("historyresult_extract.json");

        fragment.new HistoryFragmentCallback().handleResponse(message);

        assertEquals(1, fragment.getHistoryItems().size());
        HistoryInfo historyInfo = fragment.getHistoryItems().get(0);
        assertEquals("Unpacking: 25/30", historyInfo.getMessage());
    }

    @Test
    public void testHandleHistoryResult_verify() throws Exception {
        String message = Utils.readFileToString("historyresult_verify.json");

        fragment.new HistoryFragmentCallback().handleResponse(message);

        assertEquals(1, fragment.getHistoryItems().size());
        HistoryInfo historyInfo = fragment.getHistoryItems().get(0);
        assertEquals("Verifying: 16/30", historyInfo.getMessage());
    }

    @Test
    public void testHandleHistoryResult_failed() throws Exception {
    	String message = Utils.readFileToString("historyresult_failed.json");

        fragment.new HistoryFragmentCallback().handleResponse(message);

        assertEquals(3, fragment.getHistoryItems().size());
        HistoryInfo historyInfo = fragment.getHistoryItems().get(2);
        assertEquals("Download failed - Out of your server's retention?", historyInfo.getMessage());
    }
}
