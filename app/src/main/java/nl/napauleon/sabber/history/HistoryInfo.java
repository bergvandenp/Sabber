package nl.napauleon.sabber.history;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryInfo {
	private String item;
	private Long dateDownloaded;
    private CompletedStatus status;
	
	public HistoryInfo(String item, Long dateDownloaded, CompletedStatus status) {
		this.item = item;
		this.dateDownloaded = dateDownloaded;
        this.status = status;
	}
	
	public String getDateDownloaded() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(dateDownloaded*1000L));
	}

	public String getItem() {
		return item;
	}

    public CompletedStatus getStatus() {
        return status;
    }
}
