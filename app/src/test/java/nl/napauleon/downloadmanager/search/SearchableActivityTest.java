package nl.napauleon.downloadmanager.search;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

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
//		SearchNzbsTask searchNzbsTask = activity.new SearchNzbsTask();
//		File file = FileUtils.toFile(ClassLoader.getSystemClassLoader().getResource("searchresult.xml"));
//		String searchresult = FileUtils.readFileToString(file);
//		searchNzbsTask.processResult(searchresult);
//		System.out.println(activity.getResults().get(0).getTitle());
	}
	
}
