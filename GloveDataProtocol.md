# Glove Data Layout #

| **Byte Starting Address** | **Size (in bytes)** | Description |
|:--------------------------|:--------------------|:------------|
| 0 | 6 | MAC address of AP that we are Associated with (for location) |
| 6 | 1 | Channel we are on. |
| 7 | 1 | RSSI |
| 8 | 2 | local TCP port# (for connecting into the Wifly device ) |
| 10 | 4 | RTC value (MSB first to LSB last) |
| 14 | 2 | Battery Voltage on Pin 20 in millivolts (2755 for example) |
| 16 | 2 | value of the GPIO pins |
| 18 | 13 | ASCII time |
| 32 | 26 | Version string with date code |
| 60 | 32 | Programmable Device ID string (set option deviceid 

&lt;string&gt;

) |
| 92 | 2 | Boot time in milliseconds. |
| 94 | 16 | Voltage readings of Sensors 0 thru 7 (enabled with “set opt format 

&lt;mask&gt;

” ) |