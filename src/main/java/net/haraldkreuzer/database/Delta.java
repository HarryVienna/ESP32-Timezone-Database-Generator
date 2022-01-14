package net.haraldkreuzer.database;

public class Delta {
    private Integer deltaLatitude;    // nördliche oder südliche Entfernung eines Punktes vom Äquator von −90° am Südpol über 0° am Äquator bis +90° am Nordpol.
    private Integer deltalongitude;   // Position östlich oder westlich einer definierten Nord-Süd-Linie zwischen −180° und +180°

    public Delta(Integer deltaLatitude, Integer deltalongitude) {
        this.deltaLatitude = deltaLatitude;
        this.deltalongitude = deltalongitude;
    }

    public Integer getDeltaLatitude() {
        return deltaLatitude;
    }

    public void setDeltaLatitude(Integer deltaLatitude) {
        this.deltaLatitude = deltaLatitude;
    }

    public Integer getDeltalongitude() {
        return deltalongitude;
    }

    public void setDeltalongitude(Integer deltalongitude) {
        this.deltalongitude = deltalongitude;
    }
}
