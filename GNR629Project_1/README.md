# GNR629 Project 1

## Aim
* Develop an integrated Client-Server based Interoperable GIS system
* An AJAX-Driven Web Client to access and retrieve geospatial data from a WMS/WFS/WCS Server
* The Web Client should have the following functionalities 
    * Able to send various requests ( e.g. GetCapabilities, GetMap, etc) to a server (Geoserver that is installed on your machine)
	* Ability to capture the response from the server and display: 
		1. The XML response document. 
		2. Parse the XML and display only the values.
	* Ability to display multiple layers ( i.e. one on top of the other)
	* Should have a base layer ( i.e. Google Map)
	* Ability to send request to external WMS/WFS/WCS servers and display the result.
* The client GUI should have the following capabilities:
	* Ability to select layers, SRS, Bounding coordinates, format size.
* Finally the client should be aesthetically good

### Server Development
Apache Tomcat, Geoserver

### Client Development
Coding: Eclipse IDE, Java, Google Web Tool Kit (GWT),
GWTOpenlayers API.
Spatial Database: Postgresql + PostGIS

### Outline
Installation
• Install Apache Tomcat and Geoserver
• Install Eclipse IDE, GWT plugin for Eclipse
• Download and install GWTOpenlayers
• Install Postgresql first and then Install PostGIS (make sure it is
compatible with the specific version of geolayers that you are
using)

