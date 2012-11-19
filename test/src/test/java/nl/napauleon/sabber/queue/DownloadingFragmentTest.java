package nl.napauleon.sabber.queue;

import android.os.Message;
import nl.napauleon.sabber.CustomTestRunner;
import nl.napauleon.sabber.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(CustomTestRunner.class)
public class DownloadingFragmentTest {

    DownloadingFragment fragment;

    @Before
    public void setUp() throws Exception {
        fragment = new DownloadingFragment();
        fragment.onCreate(null);
    }

    @Test
    public void testHandleQueueResult() throws Exception {
        String message = Utils.readFileToString("queueresult");

        fragment.new DownloadingCallback().handleResponse(message);

        List<QueueInfo> result = fragment.getQueueItems();
        assertEquals(1, result.size());
        assertEquals("Rookie.Blue.S03E02.720p.HDTV.x264-IMMERSE", result.get(0).getItem());
    }
}
