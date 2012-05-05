package nl.napauleon.downloadmanager.search;

/**
 * Created by IntelliJ IDEA.
 * User: napauleon
 * Date: 2/29/12
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class NzbInfo {
    private String title, link, size;
    
    public NzbInfo(String title, String link, String size) {
        this.title = title;
        this.link = link;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getSize() {
        return size;
    }

    @Override
    public String toString() {
        return title;
    }
}
