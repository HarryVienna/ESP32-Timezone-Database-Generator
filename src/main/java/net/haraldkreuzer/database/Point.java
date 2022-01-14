package net.haraldkreuzer.database;

public class Point {
    private Integer latitude;    // nördliche oder südliche Entfernung eines Punktes vom Äquator von −90° am Südpol über 0° am Äquator bis +90° am Nordpol.
    private Integer longitude;   // Position östlich oder westlich einer definierten Nord-Süd-Linie zwischen −180° und +180°

    public Point(Integer latitude, Integer longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getLatitude() {
        return latitude;
    }

    public void setLatitude(Integer latitude) {
        this.latitude = latitude;
    }

    public Integer getLongitude() {
        return longitude;
    }

    public void setLongitude(Integer longitude) {
        this.longitude = longitude;
    }

    public void add(Delta delta) {
        this.latitude += delta.getDeltaLatitude();
        this.longitude += delta.getDeltalongitude();
    }
}
