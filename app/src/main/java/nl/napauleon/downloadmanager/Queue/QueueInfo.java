package nl.napauleon.downloadmanager.Queue;

public class QueueInfo {

    private String id;
	private String item;
	private String timeleft;
	private Integer percentage;
	
	public QueueInfo(String id, String item, String timeleft, Integer percentage) {
        this.id = id;
		this.item = item;
		this.timeleft = timeleft;
		this.percentage = percentage;
	}

	public String getItem() {
		return item;
	}
	
	public String getTimeleft() {
		return this.timeleft;
	}
	
	public Integer getPercentage() {
		return this.percentage;
	}

    public String getId() {
        return id;
    }
}
