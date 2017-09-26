import csv

with open('output-flightaware*.csv', 'wb') as outcsv:
    writer = csv.writer(outcsv)
    writer.writerow(["type", "ident", "lat","lon", "clock", "updateType","id", "air_ground", "hexid","alt", "gs", "heading","rp1lat", "rp1lon", "rp1alt","rp1clock", "fob", "oat","airspeed_kts", "airspeed_mach", "winds","eta", "baro_alt", "gps-alt","atcident", "reg", "squawk","altChange", "geometry"])