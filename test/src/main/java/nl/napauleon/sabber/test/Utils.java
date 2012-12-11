package nl.napauleon.sabber.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Utils {

    static public String readFileToString(String filename) throws IOException {
        File file = FileUtils.toFile(ClassLoader.getSystemClassLoader().getResource(filename));
        return FileUtils.readFileToString(file);
    }
}
