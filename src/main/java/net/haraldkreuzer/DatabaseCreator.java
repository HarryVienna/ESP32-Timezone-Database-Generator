package net.haraldkreuzer;

import net.haraldkreuzer.database.Point;
import net.haraldkreuzer.database.*;
import org.geotools.data.FeatureReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;


public class DatabaseCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCreator.class);

    final static Byte VERSION = 1;
    final static Byte PRECISION = 24;
    final static String SIGNATURE = "TZDB";

    final static String DELIMITER = "=";




    public static void main(String[] args) throws Exception {

        DatabaseCreator databaseCreator = new DatabaseCreator();

        TimezoneDatabase timezoneDatabase = databaseCreator.create();

        databaseCreator.storeData(timezoneDatabase);
    }

    public TimezoneDatabase create() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();

        // Read all timezones
        File timeZones = new File(classLoader.getResource("timezones/timezones.conf").getFile());

        Map<String,String> timezoneMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(timeZones))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(DELIMITER);
                if (values.length == 2) {
                    if (timezoneMap.get(values[0]) == null) {
                        timezoneMap.put(values[0], values[1]);
                    } else {
                        LOGGER.error("Duplicate: {}", values[0]);
                    }
                }
            }
        }

        // Create database object
        LocalDate creationDate = LocalDate.now();

        TimezoneDatabase timezoneDatabase = new TimezoneDatabase();
        Header header = new Header();
        header.setSignature(SIGNATURE);
        header.setVersion(VERSION);
        header.setPrecision(PRECISION);
        header.setCreationDate(creationDate);
        timezoneDatabase.setHeader(header);
        List<Country> countryList = new ArrayList<>();
        timezoneDatabase.setCountryList(countryList);


        // Read all polygons
        //File fileShapes = new File(classLoader.getResource("timezones.shapefile-with-oceans/combined-shapefile-with-oceans.shp").getFile());
        File fileShapes = new File(classLoader.getResource("timezones.shapefile/combined-shapefile.shp").getFile());

        FileDataStore fileDataStore = FileDataStoreFinder.getDataStore(fileShapes);

        FeatureReader featureReader = fileDataStore.getFeatureReader();

        while (featureReader.hasNext()) {
            SimpleFeature simpleFeature = (SimpleFeature) featureReader.next();
            simpleFeature.getAttributes();
            String tzid = (String) simpleFeature.getAttribute("tzid");
            String tzValue = timezoneMap.get(tzid);

            if (tzValue == null) {
                LOGGER.error("tzid not found: {}", tzid);
                continue;
            }

            // Debug
//            if (!"Europe/Rome".equals(tzid) && !"Europe/Vatican".equals(tzid)) {
//                continue;
//            }
//            if (!"Asia/Muscat".equals(tzid)) {
//                continue;
//            }

            Country country = new Country();
            country.setTimezoneName(tzid);
            country.setTimezoneValue(tzValue);

            Geometry geometry = (Geometry) simpleFeature.getAttribute("the_geom");

            if (geometry instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) geometry;

                country.setArea(multiPolygon.getArea());

                // Bounding Box
                Envelope envelope = multiPolygon.getEnvelopeInternal();

                Integer minY = floatToFixedPoint(envelope.getMinY(), 90, PRECISION);
                Integer minX = floatToFixedPoint(envelope.getMinX(), 180, PRECISION);
                Integer maxY = floatToFixedPoint(envelope.getMaxY(), 90, PRECISION);
                Integer maxX = floatToFixedPoint(envelope.getMaxX(), 180, PRECISION);

                Point pointFrom = new Point(minY, minX);
                Point pointTo = new Point(maxY, maxX);

                BoundingBox boundingBox = new BoundingBox(pointFrom, pointTo);

                country.setBoundingBox(boundingBox);
                List<Shape> shapeList = new ArrayList<>();
                country.setShapeList(shapeList);

                // Polygons
                for (int i = 0; i< multiPolygon.getNumGeometries(); i++ ) {
                    Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

                    Coordinate[]coordinates = polygon.getExteriorRing().getCoordinates();
                    //LOGGER.debug("coordinates in polygon {} {} ", i, coordinates.length);

                    Shape shape = new Shape();
                    List<Delta> deltaList = new ArrayList<>();
                    shape.setDeltaList(deltaList);

                    Integer prev_coorY = null;
                    Integer prev_coorX = null;
                    for (Coordinate coordinate : coordinates) {

                        Integer coorY = floatToFixedPoint(coordinate.getY(), 90, PRECISION);
                        Integer coorX = floatToFixedPoint(coordinate.getX(), 180, PRECISION);

                        if (prev_coorY == null && prev_coorX == null) { // First point in polygon
                            shape.setStart(new Point(coorY, coorX));
                        }
                        else if (coorX.equals(prev_coorX) && coorY.equals(prev_coorY)){ // Same value, ignore
                            continue;
                        }
                        else {
                            Integer diffY = coorY - prev_coorY;
                            Integer diffX = coorX - prev_coorX;

                            Delta delta = new Delta(diffY, diffX);

                            deltaList.add(delta);
                        }

                        prev_coorY = coorY;
                        prev_coorX = coorX;
                    }
                    shapeList.add(shape);
                }


            }
            countryList.add(country);
        }

        return timezoneDatabase;
    }

    public void storeData(TimezoneDatabase timezoneDatabase) {

        ByteBuffer buffer = timezoneDatabase.generateBuffer();


        try {

            File outputFolder = new File(this.getClass().getResource("/").getPath() + "output/");
            outputFolder.mkdirs();

            File outputFile = new File(outputFolder, "timezones.bin");
            outputFile.delete();
            if (outputFile.createNewFile()) {

                FileChannel wChannel = new FileOutputStream(outputFile, false).getChannel();
                wChannel.write(buffer);
                wChannel.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    int floatToFixedPoint(double input, double scale, int precision) {
        double inputScaled = input / scale;
        return (int) Math.round(inputScaled * Math.pow(2, precision-1));
    }

    double fixedPointToFloat(int input, double scale, int precision) {
        double value = (double)(input / (double)(1 << (precision - 1)));
        return  value * scale;
    }
}
