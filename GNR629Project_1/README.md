# GNR629 Project 1

## Outline
* An integrated Client-Server based Interoperable GIS system
* An AJAX-Driven Web Client to access and retrieve geospatial data from a WMS/WFS/WCS Server
* The Web Client is having the following functionalities 
    * Ability to send various requests ( e.g. GetCapabilities, GetMap, etc) to a server (Geoserver that is installed on your machine)
	* Ability to capture the response from the server and display: 
		1. The XML response document. 
		2. Parse the XML and display the necessary values.
	* Ability to display multiple layers ( i.e. one on top of the other)
	* Has a Google Map as a base layer
	* Ability to send request to external WMS/WFS/WCS servers (in this case it's the NASA Socio Economic Data) and display the result.
* The client GUI is having the following capabilities:
	* Ability to select layers, SRS, Bounding coordinates, format size.
* Finally the client is aesthetically good and responsive

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

