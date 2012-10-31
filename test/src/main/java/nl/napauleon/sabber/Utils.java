package nl.napauleon.sabber;

import android.os.Message;
import nl.napauleon.sabber.http.HttpGetHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Utils {
    static public Message createResultMessage(String filename) throws IOException {
        Message message = new Message();
        message.what = HttpGetHandler.MSG_RESULT;
        message.obj = readFileToString(filename);
        return message;
    }

    static public String readFileToString(String filename) throws IOException {
        File file = FileUtils.toFile(ClassLoader.getSystemClassLoader().getResource(filename));
        return FileUtils.readFileToString(file);
    }
}
