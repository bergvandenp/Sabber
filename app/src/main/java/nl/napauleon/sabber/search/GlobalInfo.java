package nl.napauleon.sabber.search;

public class GlobalInfo {

    String timeleft, size, speed, eta;

    public GlobalInfo(String timeleft, String size, String speed, String eta) {
        this.timeleft = timeleft;
        this.size = size;
        this.speed = speed;
        this.eta = eta;
    }

    public String getTimeleft() {
        return timeleft;
    }

    public String getSize() {
        return size;
    }

    public String getSpeed() {
        return speed;
    }

    public String getEta() {
        return eta;
    }
}
