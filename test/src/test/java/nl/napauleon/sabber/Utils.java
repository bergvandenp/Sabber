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
        File file = FileUtils.toFile(ClassLoader.getSystemClassLoader().getResource(filename));
        message.obj = FileUtils.readFileToString(file);
        return message;
    }
}
