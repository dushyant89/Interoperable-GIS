# GNR629 Project 2

## Outline
* A SOS database and AJAX-based client for Sensor Observation Service (SOS).
* Ability to create subsets of SOS data
* Spatial subsetting (bounding box) 
* Temporal subsetting (After a time instant, during time instant, etc.)
* Ability to display the sensors data on the Google Map as markers
* External servers used for sensor observations are
	* National Data Buoy Center
	* National Data for Tides and Currents

### Server Development
Apache Tomcat, Geoserver

### Client Development
* Coding: Eclipse IDE, Java, Google Web Tool Kit (GWT), GWTOpenlayers API.
* Spatial Database: Postgresql + PostGIS

### Installation
* Install Apache Tomcat and Geoserver
* Install Eclipse IDE, GWT plugin for Eclipse
* Download and install GWTOpenlayers
* Make sure to run the Google Chrome in unsecure mode for sending requests to external OGC web services compliant servers
* Install Postgresql first and then Install PostGIS (make sure it is compatible with the specific version of geolayers that you are using)

