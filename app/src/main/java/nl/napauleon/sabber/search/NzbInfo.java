package nl.napauleon.sabber.search;

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
