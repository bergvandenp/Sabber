package nl.napauleon.sabber.search;

import android.os.Message;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import nl.napauleon.sabber.http.HttpGetHandler;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SearchableActivityTest {

	private SearchableActivity activity;

	@Before
    public void setUp() throws Exception {
        activity = new SearchableActivity();
        activity.onCreate(null);
    }
	
	@Test
	public void testProcessResult() throws IOException {
        //TODO: fixen!
//        SearchableActivity.SearchHandler searchHandler = activity.new SearchHandler();
        File file = FileUtils.toFile(ClassLoader.getSystemClassLoader().getResource("searchresult.xml"));
        Message message = new Message();
        message.what = HttpGetHandler.MSG_RESULT;
        message.obj = FileUtils.readFileToString(file);
//        searchHandler.handleMessage(message);

        assertEquals(4, activity.getResults().size());
		assertTrue(activity.getResults().get(0).getTitle().startsWith("<kere.ws> - TV - 1334088173 -"));
	}
	
}
