package net.haraldkreuzer.database;

import java.util.List;

public class Country {

    private String timezoneName;
    private String timezoneValue;
    private BoundingBox boundingBox;
    private List<Shape> shapeList;
    private Double area;

    public Country() {
    }

    public String getTimezoneName() {
        return timezoneName;
    }

    public void setTimezoneName(String timezoneName) {
        this.timezoneName = timezoneName;
    }

    public String getTimezoneValue() {
        return timezoneValue;
    }

    public void setTimezoneValue(String timezoneValue) {
        this.timezoneValue = timezoneValue;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public List<Shape> getShapeList() {
        return shapeList;
    }

    public void setShapeList(List<Shape> shapeList) {
        this.shapeList = shapeList;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }
}
