package net.haraldkreuzer;

import net.haraldkreuzer.database.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DatabaseCreatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCreatorTest.class);

    @Test
    public void testCreateDatabase() throws Exception {
        DatabaseCreator databaseCreator = new DatabaseCreator();

        TimezoneDatabase timezoneDatabase = databaseCreator.create();

        List<Country> countryList = timezoneDatabase.getCountryList();

        //PrintWriter writerLength = new PrintWriter("c:/temp/deltasLength.txt");
        //PrintWriter writerWidth = new PrintWriter("c:/temp/deltasWidth.txt");


        // Statistik
        Integer totalCountries = 0;
        Integer totalShapes = 0;
        Integer maxDelta = 0;
        Integer totalDeltas = 0;
        Integer totalDeltasSmall = 0;
        Integer totalDeltasSMedium = 0;
        Integer totalDeltasLarge = 0;
        Integer maxPolygenLength = 0;
        String maxPolygenCountry = "";


        for (Country country: countryList) {
            totalCountries++;
            List<Shape> shapeList = country.getShapeList();
            for(Shape shape:shapeList) {
                totalShapes++;
                List<Delta> deltaList = shape.getDeltaList();
                if (deltaList.size() > maxPolygenLength) {
                    maxPolygenLength = deltaList.size();
                    maxPolygenCountry = country.getTimezoneName();
                    LOGGER.debug(maxPolygenCountry + " " + maxPolygenLength);
                }
                for (Delta delta: deltaList) {
                    //writerLength.print(delta.getDeltaLatitude() + " ");
                    //writerWidth.print(delta.getDeltalongitude() + " ");

                    totalDeltas += 2;
                    if (Math.abs(delta.getDeltaLatitude()) > maxDelta ) {
                        maxDelta = Math.abs(delta.getDeltaLatitude());
                    }
                    if (Math.abs(delta.getDeltalongitude()) > maxDelta ) {
                        maxDelta = Math.abs(delta.getDeltalongitude());
                    }

                    if (Math.abs(delta.getDeltaLatitude()) <= 127 ) {
                        totalDeltasSmall++;
                    }
                    if (Math.abs(delta.getDeltalongitude()) <= 127 ) {
                        totalDeltasSmall++;
                    }

                    if (Math.abs(delta.getDeltaLatitude()) > 127 &&  Math.abs(delta.getDeltaLatitude()) < 32767) {
                        totalDeltasSMedium++;
                    }
                    if (Math.abs(delta.getDeltalongitude()) > 127 &&  Math.abs(delta.getDeltalongitude()) < 32767) {
                        totalDeltasSMedium++;
                    }

                    if (Math.abs(delta.getDeltaLatitude()) >= 32767 ) {
                        totalDeltasLarge++;
                    }
                    if (Math.abs(delta.getDeltalongitude()) >= 65536 ) {
                        totalDeltasLarge++;
                    }
                }
            }
        }

        //writerLength.close();
        //writerWidth.close();

        //LOGGER.debug("Total values: {}", totalValues * 2);
        LOGGER.debug("Total countries: {}", totalCountries);
        LOGGER.debug("Total shapes: {}", totalShapes);
        LOGGER.debug("Max delta: {}", maxDelta);
        LOGGER.debug("Total deltas: {}", totalDeltas);
        LOGGER.debug("Total deltas small: {}", totalDeltasSmall);
        LOGGER.debug("Total deltas medium: {}", totalDeltasSMedium);
        LOGGER.debug("Total deltas large: {}", totalDeltasLarge);
        LOGGER.debug("Max polygen length: {}", maxPolygenLength);
        LOGGER.debug("Max polygen country: {}", maxPolygenCountry);
    }

    @Test
    public void testAccurracy() throws Exception {
        DatabaseCreator databaseCreator = new DatabaseCreator();

        TimezoneDatabase timezoneDatabase = databaseCreator.create();

        List<Country> countryList = timezoneDatabase.getCountryList();
        for (Country country: countryList) {
            //if ("Europe/Berlin".equals(country.getTimezoneName())) {
                List<Shape> shapeList = country.getShapeList();
                for(Shape shape:shapeList) {
                    //if (shape.getDeltaList().size() > 150000) {
                        Point start = shape.getStart();
                        Point end = new Point(start.getLatitude(), start.getLongitude());
                        List<Delta> deltaList = shape.getDeltaList();
                        for (Delta delta: deltaList) {
                            end.add(delta);
                        }
                        if (!start.getLongitude().equals(end.getLongitude()) || !start.getLatitude().equals(end.getLatitude())) {
                            LOGGER.debug("Country: {} {} ", country.getTimezoneName(), deltaList.size());
                            LOGGER.debug("Start: {} {}", start.getLatitude(), start.getLongitude());
                            LOGGER.debug("End  : {} {}", end.getLatitude(), end.getLongitude());
                        }
                    //}
                }
            //}
        }
    }

    @Test
    public void testCreateTestDb() throws Exception {


        Header header = new Header();
        header.setVersion((byte)1);
        header.setSignature("TZDB");
        header.setPrecision((byte)24);
        header.setCreationDate(LocalDate.now());


        Country country1 = getCountry1();
        Country country2 = getCountry2();
        Country country3 = getCountryAustria();

        List<Country> countryList = new ArrayList<>();
        countryList.add(country1);
        countryList.add(country2);
        countryList.add(country3);




        TimezoneDatabase timezoneDatabase = new TimezoneDatabase();

        timezoneDatabase.setHeader(header);
        timezoneDatabase.setCountryList(countryList);


        DatabaseCreator databaseCreator = new DatabaseCreator();

        databaseCreator.storeData(timezoneDatabase);


    }

    private Country getCountry1() {
        Point start = new Point(1234,-1234);

        Delta delta0 = new Delta(640,-509);
        Delta delta1 = new Delta(640,-509);
        Delta delta2 = new Delta(640,-509);
        Delta delta3 = new Delta(640,-509);
        Delta delta4 = new Delta(636,-466);
        Delta delta5 = new Delta(-100000,-100000);
        Delta delta6 = new Delta(-10000,-10000);
        Delta delta7 = new Delta(-1000,-1000);
        Delta delta8 = new Delta(-100,-100);
        Delta delta9 = new Delta(-10,-10);

        List<Delta> deltaList = new ArrayList<>();
        deltaList.add(delta0);
        deltaList.add(delta1);
        deltaList.add(delta2);
        deltaList.add(delta3);
        deltaList.add(delta4);
        deltaList.add(delta5);
        deltaList.add(delta6);
        deltaList.add(delta7);
        deltaList.add(delta8);
        deltaList.add(delta9);

        Shape shape = new Shape();
        shape.setStart(start);
        shape.setDeltaList(deltaList);

        List<Shape> shapeList = new ArrayList<>();
        shapeList.add(shape);

        Point from = new Point(1234,-1234);
        Point to = new Point(112344,109876);
        BoundingBox boundingBox = new BoundingBox(from, to);

        Country country = new Country();
        country.setTimezoneName("Europe/Berlin");
        country.setTimezoneValue("CET-1CEST,M3.5.0,M10.5.0/3");
        country.setArea(666.0);
        country.setBoundingBox(boundingBox);
        country.setShapeList(shapeList);
        return country;
    }

    private Country getCountry2() {
        Point start1 = new Point(200000,-200000);

        Delta delta_1_0 = new Delta(11,11);
        Delta delta_1_1 = new Delta(111,111);
        Delta delta_1_2 = new Delta(1111,1111);
        Delta delta_1_3 = new Delta(11111,11111);
        Delta delta_1_31 = new Delta(11,11);
        Delta delta_1_4 = new Delta(111111,111111);
        Delta delta_1_5 = new Delta(-111111,-111111);
        Delta delta_1_6 = new Delta(-11111,-11111);
        Delta delta_1_7 = new Delta(-1111,-1111);
        Delta delta_1_8 = new Delta(-111,-111);
        Delta delta_1_9 = new Delta(-11,-11);

        List<Delta> deltaList1 = new ArrayList<>();
        deltaList1.add(delta_1_0);
        deltaList1.add(delta_1_1);
        deltaList1.add(delta_1_2);
        deltaList1.add(delta_1_3);
        deltaList1.add(delta_1_31);
        deltaList1.add(delta_1_4);
        deltaList1.add(delta_1_5);
        deltaList1.add(delta_1_6);
        deltaList1.add(delta_1_7);
        deltaList1.add(delta_1_8);
        deltaList1.add(delta_1_9);

        Shape shape1 = new Shape();
        shape1.setStart(start1);
        shape1.setDeltaList(deltaList1);



        Point start2 = new Point(190000,-190000);

        Delta delta_2_0 = new Delta(11,11);
        Delta delta_2_1 = new Delta(111,111);
        Delta delta_2_2 = new Delta(1111,1111);
        Delta delta_2_3 = new Delta(-1111,-1111);
        Delta delta_2_4 = new Delta(-111,-111);
        Delta delta_2_5 = new Delta(-11,-11);

        List<Delta> deltaList2 = new ArrayList<>();
        deltaList2.add(delta_2_0);
        deltaList2.add(delta_2_1);
        deltaList2.add(delta_2_2);
        deltaList2.add(delta_2_3);
        deltaList2.add(delta_2_4);
        deltaList2.add(delta_2_5);


        Shape shape2 = new Shape();
        shape2.setStart(start2);
        shape2.setDeltaList(deltaList2);



        List<Shape> shapeList = new ArrayList<>();
        shapeList.add(shape1);
        shapeList.add(shape2);

        Point from = new Point(200000,-200000);
        Point to = new Point(323455,-76545);
        BoundingBox boundingBox = new BoundingBox(from, to);

        Country country = new Country();
        country.setTimezoneName("Europe/Test");
        country.setTimezoneValue("CET-1CEST,M3.5.0,M10.5.0/3");
        country.setArea(777.0);
        country.setBoundingBox(boundingBox);
        country.setShapeList(shapeList);
        return country;
    }

    private Country getCountryAustria() {
        Point start = new Point(4406629,445155);

        Delta delta0 = new Delta(34953                ,142607        );
        Delta delta1 = new Delta(-20692                ,21018        );
        Delta delta2 = new Delta(78200                ,3076        );
        Delta delta3 = new Delta(38122                ,177838        );
        Delta delta4 = new Delta(-87335                ,3402        );
        Delta delta5 = new Delta(-113526                ,-146940        );
        Delta delta6 = new Delta(70278                ,-201001        );


        List<Delta> deltaList = new ArrayList<>();
        deltaList.add(delta0);
        deltaList.add(delta1);
        deltaList.add(delta2);
        deltaList.add(delta3);
        deltaList.add(delta4);
        deltaList.add(delta5);
        deltaList.add(delta6);

        Shape shape = new Shape();
        shape.setStart(start);
        shape.setDeltaList(deltaList);

        List<Shape> shapeList = new ArrayList<>();
        shapeList.add(shape);

        Point from = new Point(4336351                ,    445155    );
        Point to = new Point(4537212               ,   793096      );
        BoundingBox boundingBox = new BoundingBox(from, to);

        Country country = new Country();
        country.setTimezoneName("Europe/Vienna");
        country.setTimezoneValue("CET-1CEST,M3.5.0,M10.5.0/3");
        country.setArea(666.0);
        country.setBoundingBox(boundingBox);
        country.setShapeList(shapeList);
        return country;
    }
}
