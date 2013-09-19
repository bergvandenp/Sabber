package nl.napauleon.sabber.http;

import android.content.SharedPreferences;
import com.xtremelabs.robolectric.shadows.ShadowPreferenceManager;
import nl.napauleon.sabber.CustomTestRunner;
import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.Utils;
import nl.napauleon.sabber.search.NzbInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(CustomTestRunner.class)
public class NzbSearchConnectionHelperTest {

    NzbSearchConnectionHelper helper;

    @Before
    public void setUp() throws Exception {
        SharedPreferences preferences = ShadowPreferenceManager.getDefaultSharedPreferences(new MainActivity());
        helper = new NzbSearchConnectionHelper(preferences);
    }

    @Test
    public void testParseResults() throws IOException, SAXException, ParserConfigurationException {
        String resultString = Utils.readFileToString("result");
        List<NzbInfo> results = helper.parseResults(resultString);

        assertEquals(4, results.size());

    }
}
