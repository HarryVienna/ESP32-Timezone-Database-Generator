package net.haraldkreuzer.database;

import java.util.ArrayList;
import java.util.List;

public class Shape {

    Point start;
    List<Delta> deltaList;

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public List<Delta> getDeltaList() {
        return deltaList;
    }

    public void setDeltaList(List<Delta> deltaList) {
        this.deltaList = deltaList;
    }
}
