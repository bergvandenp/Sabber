package nl.napauleon.sabber;


import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;

public class CustomTestRunner extends RobolectricTestRunner {
    public CustomTestRunner(Class testClass) throws InitializationError {
        // defaults to "AndroidManifest.xml", "res" in the current directory
        super(testClass, new File("../app/"));
    }
}
