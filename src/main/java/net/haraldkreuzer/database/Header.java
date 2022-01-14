package net.haraldkreuzer.database;

import java.time.LocalDate;

public class Header {

    String signature;
    Byte version;
    Byte precision;
    LocalDate creationDate;

    public Header() {
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Byte getVersion() {
        return version;
    }

    public void setVersion(Byte version) {
        this.version = version;
    }

    public Byte getPrecision() {
        return precision;
    }

    public void setPrecision(Byte precision) {
        this.precision = precision;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
}
