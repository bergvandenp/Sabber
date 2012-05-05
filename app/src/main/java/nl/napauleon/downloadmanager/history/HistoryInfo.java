package nl.napauleon.downloadmanager.history;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryInfo {
	private String item;
	private Long dateDownloaded;
	
	public HistoryInfo(String item, Long dateDownloaded) {
		this.item = item;
		this.dateDownloaded = dateDownloaded;
	}
	
	public String getDateDownloaded() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(dateDownloaded*1000L));
	}

	public String getItem() {
		return item;
	}
}
