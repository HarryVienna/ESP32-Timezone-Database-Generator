package net.haraldkreuzer.database;

import net.haraldkreuzer.DatabaseCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimezoneDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimezoneDatabase.class);

    final static int BUFFER_SIZE = 16777216; // 2^24

    private Header header;
    private List<Country> countryList = new ArrayList<>();

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<Country> getCountryList() {
        return countryList;
    }

    public void setCountryList(List<Country> countryList) {
        this.countryList = countryList;
    }

    public ByteBuffer generateBuffer() {

        // Smallest countries first
        this.getCountryList().sort(new Comparator<Country>() {
            @Override
            public int compare(Country o1, Country o2) {
                return o1.getArea().compareTo(o2.getArea());
            }
        });

        ByteBuffer buffer = ByteBuffer.allocate( BUFFER_SIZE );
        buffer.order(ByteOrder.LITTLE_ENDIAN);


        Header header = this.getHeader();

        buffer.put(header.getVersion());
        buffer.put(header.getSignature().getBytes());
        buffer.put(header.getPrecision());
        buffer.put(header.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).getBytes());

        buffer.position(32);
        List<Country> countryList = this.getCountryList();

        Integer size = countryList.size();
        buffer.putInt(size);

        Map<String, Integer> positionOfCountryIndexMap = new HashMap<>();
        for (Country country: countryList) {


            String tzName = country.getTimezoneName();
            ByteBuffer tzNameBuffer = ByteBuffer.allocate(64);
            tzNameBuffer.put(tzName.getBytes());
            tzNameBuffer.put((byte) 0x00);
            tzNameBuffer.position(0);
            buffer.put(tzNameBuffer);

            String tzValue = country.getTimezoneValue();
            ByteBuffer tzValueBuffer = ByteBuffer.allocate(64);
            tzValueBuffer.put(tzValue.getBytes());
            tzValueBuffer.put((byte) 0x00);
            tzValueBuffer.position(0);
            buffer.put(tzValueBuffer);

            Integer positionOfCountryIndex = buffer.position(); // Remember position of index in TOC
            positionOfCountryIndexMap.put(tzName, positionOfCountryIndex);

            buffer.position(positionOfCountryIndex + 4); // Reserve 4 bytes for pointer to country data
        }


        for (Country country: countryList) {

            String tzName = country.getTimezoneName();
            LOGGER.debug("Timezone: {}", tzName);

            Integer positionOfCountryIndex = positionOfCountryIndexMap.get(tzName);

            Integer currentCountryPosition = buffer.position();   // Store current position in TOC
            buffer.position(positionOfCountryIndex);

            buffer.putInt(currentCountryPosition);
            buffer.position(currentCountryPosition);

            BoundingBox boundingBox = country.getBoundingBox();
            Point from = boundingBox.getFrom();
            Point to = boundingBox.getTo();
            buffer.putInt(from.getLatitude());
            buffer.putInt(from.getLongitude());
            buffer.putInt(to.getLatitude());
            buffer.putInt(to.getLongitude());

            List<Shape> shapeList = country.getShapeList();
            buffer.putInt(shapeList.size());

            Map<Shape, Integer> positionOfShapeIndexMap = new HashMap<>();
            for(Shape shape:shapeList) {
                Integer positionOfShapeIndex = buffer.position(); // Remember position of index in TOC
                positionOfShapeIndexMap.put(shape, positionOfShapeIndex);

                buffer.position(positionOfShapeIndex + 4); // Reserve 4 bytes for pointer to country data
            }

            for(Shape shape:shapeList) {
                Integer positionOfShapeIndex = positionOfShapeIndexMap.get(shape);

                Integer currentShapePosition = buffer.position();   // Store current position in TOC
                buffer.position(positionOfShapeIndex);
                buffer.putInt(currentShapePosition);
                buffer.position(currentShapePosition);

                Point start = shape.getStart();
                buffer.putInt(start.getLatitude());
                buffer.putInt(start.getLongitude());

                List<Delta> deltaList = shape.getDeltaList();
                buffer.putInt(deltaList.size());

                for (Delta delta: deltaList) {
                    Integer deltaLatitude = delta.getDeltaLatitude();
                    Integer deltalongitude = delta.getDeltalongitude();

                    writeValue(deltaLatitude, buffer);

                    writeValue(deltalongitude, buffer);
                }
            }
        }

        buffer.flip();

        return buffer;
    }

    private void writeValue(Integer value, ByteBuffer buffer) {
        // -128 = 0x80 and 127 = 0x7F are reserved as marker
        if (value >= -127 && value <= 126) {
            buffer.put(value.byteValue());
        }
        else if (value >= -32768 && value <= 32767) {
            buffer.put((byte)0x80);
            buffer.putShort(value.shortValue());
        }
        else {
            buffer.put((byte)0x7F);
            buffer.putInt(value);
        }
    }

}
