# Timezone Database Generator

This Java project generates a compressed lookup database of all timezones. The aim is, to be exact 
as possible but generate a file that fits in a 128 MBit W25Q128 SpiFlash chip. 

The shape databases are obtained from [here](https://github.com/evansiroky/timezone-boundary-builder) 
and converted to the format used by the ESP32 timezone finder
 
A configuration file provides the mapping from timezone names to timezone values like
* Europe/Berlin => CET-1CEST,M3.5.0,M10.5.0/3

The compression is done in a very simple way. The floating numbers are converted to an integer
format with a specified precision. Then for each polygon only the first value is stored. All following
points are stored as difference to the previous point. To save space, the difference is stored as
one byte for values between -127 and 126. The values -128 and 127 are reserved as marker that there are following
bytes for longer values. So values between -32768 and 32767 are stored with a beginning marker 0x80 and
then two bytes for the value. Bigger values use the marker 0x7F and then 4 bytes for the 
value.

This works because statistically most values are in the smallest range:
- Total deltas: 12198884
- Total deltas small: 10568754
- Total deltas medium: 1629492
- Total deltas large: 638

Before generating the database, the shapefile must be downloaded. This is done with the maven goal download:wget
Then just start the main class. The output file is storde in the target directory under  
