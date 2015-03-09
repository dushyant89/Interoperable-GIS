
package com.csre.gnr.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.StyleMap;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MouseDefaults;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.MapClickListener;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureSelectedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3MapType;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3Options;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.popup.FramedCloud;
import org.gwtopenmaps.openlayers.client.popup.Popup;
import org.gwtopenmaps.openlayers.client.strategy.ClusterStrategy;
import org.gwtopenmaps.openlayers.client.strategy.Strategy;
import org.gwtopenmaps.openlayers.client.util.Attributes;
import org.gwtopenmaps.openlayers.client.util.JObjectArray;
import org.gwtopenmaps.openlayers.client.util.JSObject;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GNR629_Project2 implements EntryPoint {

    private static final Projection DEFAULT_PROJECTION = new Projection("EPSG:4326");
    public static MapWidget mapWidget;
    public static LayoutPanel panelForMap = new LayoutPanel();
    public static LinkedHashMap<String,String> server=new LinkedHashMap<String,String>();
    public static ListBox serverList;
    public static Map map;
    private static TabPanel servicePanel = new TabPanel();
    private static VerticalPanel panelForOperations;
    private static VerticalPanel panelForFilterting;
    public  static HorizontalPanel topRow=new HorizontalPanel();
    public static final ArrayList<String> stationsLatLonList=new ArrayList<String>();
    public static final ArrayList<String> stationNamesList=new ArrayList<String>();
    public static boolean filterTabSelected=false;
    public static Vector stationsLayer;
    public static GetObservation gb=new GetObservation();
    public static ListBox observedPropertyList=new ListBox();
    public static ListBox offeringsList=new ListBox();
    public static NodeList offerings;
    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        //setting the default projection
        mapWidget = new MapWidget("100%","623px", setMapOtpions("EPSG:4326"));

        setMap();
        
        //adding layer to the dock panel
        panelForMap.add(mapWidget);
        panelForMap.setWidth("100%");
        panelForMap.setHeight("100%");
        
        Label title=new Label("GNR 629 SOS");
        title.setStyleName("gwt-title");
        panelForMap.add(title);
        panelForMap.setWidgetVerticalPosition(title,Layout.Alignment.BEGIN);
        panelForMap.setWidgetLeftWidth(title, 43, Unit.PCT, 150, Unit.PX);
        panelForMap.setWidgetTopHeight(title, 3, Unit.PCT, 50, Unit.PX);

        //setting up the server
        //server.put("Local","http://localhost:8083/geoserver/");
        server.put("National Data Buoy Center", "http://sdf.ndbc.noaa.gov/sos/server.php?service=SOS");
        server.put("National Data for Tides and Currents", "http://opendap.co-ops.nos.noaa.gov/ioos-dif-sos/SOS?service=SOS");
        
        //Getting the getCapabilities content for SOS for a default server 
        RequestBuilder builderForOperations=new RequestBuilder(RequestBuilder.GET, "http://sdf.ndbc.noaa.gov/sos/server.php?request=GetCapabilities&service=SOS");
        
        //adding the verticals panels to the tab bar
        panelForOperations=prepareOperationsPanel(builderForOperations,"National Data Buoy Center");
        servicePanel.add(changeConfigurationPanel(),"Configuration");
        servicePanel.add(panelForOperations,"View Data");
        panelForFilterting=prepareFilteringPanel();
        servicePanel.add(panelForFilterting,"Filtering");
        
        servicePanel.setAnimationEnabled(true);
        servicePanel.selectTab(0);
        
        servicePanel.addSelectionHandler(new SelectionHandler<Integer>() {
            
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                // TODO Auto-generated method stub
                if(event.getSelectedItem()==2) {
                    filterTabSelected=true;
                }
                else {
                    filterTabSelected=false;
                }
            }
        });
        
        //adding the service panel to the horizontal panel
        topRow.add(servicePanel);

        //creating the close button
        final Button hide=new Button("X");
        hide.addStyleName("gwt-Button-Secondary");
        hide.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
                if(servicePanel.isVisible()) {

                    servicePanel.setVisible(false);
                    hide.setHTML("show");
                }
                else {
                    servicePanel.setVisible(true);
                    hide.setHTML("X");
                }
            }
        });
        topRow.add(hide);

        panelForMap.add(topRow);
        panelForMap.setWidgetVerticalPosition(topRow,Layout.Alignment.BEGIN);
        panelForMap.setWidgetLeftWidth(topRow, 0, Unit.PCT, 35, Unit.PCT);
        panelForMap.setWidgetTopHeight(topRow, 0, Unit.PCT, 100, Unit.PCT);

        RootLayoutPanel rp= RootLayoutPanel.get();
        rp.add(panelForMap);

    }

    private static VerticalPanel changeConfigurationPanel() {

        final VerticalPanel configPanel=new VerticalPanel();
        configPanel.setSpacing(3);
        Label serverLabel=new Label("Select Server :");

        configPanel.add(new HTML("<p style='border-bottom:1px solid black;'>Configuration Options for Project</p>"));
        configPanel.add(serverLabel);

        serverList=new ListBox();
        for(String value:server.keySet())
        {
            serverList.addItem(value);
        }

        Button changeServerButton=new Button("Change Server");
        changeServerButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                //changing the server and fetching new get capabilities
                
                String selectedServer=serverList.getItemText(serverList.getSelectedIndex());
                RequestBuilder builder=new RequestBuilder(RequestBuilder.GET, server.get(selectedServer)+"&request=GetCapabilities");
                
                servicePanel.remove(panelForOperations);
                panelForOperations=prepareOperationsPanel(builder,selectedServer);
                servicePanel.add(panelForOperations, "Operations");
                
            }
        });

        configPanel.add(serverList);
        configPanel.add(new HTML("<br/><br/>"));
        configPanel.add(changeServerButton);

        return configPanel;
    }
    
    private static VerticalPanel prepareFilteringPanel() {
        
        final VerticalPanel panelForFiltering=new VerticalPanel();
        final HTML spatialLabel=new HTML("<p style='border-bottom:1px solid black;'>Spatial Subset</p>");
        final HTML temporalLabel=new HTML("<p style='border-bottom:1px solid black;'>Temporal Subset</p>");
        final HTML visualizeLabel=new HTML("<p style='border-bottom:1px solid black;'>Visualize</p>");
        final TextBox llonTextBox=new TextBox();
        final TextBox llatTextBox=new TextBox();
        final TextBox ulonTextBox=new TextBox();
        final TextBox ulatTextBox=new TextBox();
        
        llonTextBox.setWidth("50px");
        llonTextBox.setHeight("10px");
        llatTextBox.setWidth("50px");
        llatTextBox.setHeight("10px");
        ulonTextBox.setWidth("50px");
        ulonTextBox.setHeight("10px");
        ulatTextBox.setWidth("50px");
        ulatTextBox.setHeight("10px");
        
        final Button spatialFilterButton=new Button("Apply Spatial Filter");
        final Button temporalFilterButton=new Button("Apply Temporal Filter");
        final Button visualizeButton=new Button("Visualize");
        final Button clearFilterButton=new Button("Clear");
        clearFilterButton.addStyleName("gwt-Button-Secondary");
        
        panelForFiltering.add(spatialLabel);
        
        panelForFiltering.add(new HTML("Upper Left:<br/>"));
        HorizontalPanel panelForUpperLeft=new HorizontalPanel();
        panelForUpperLeft.add(new Label("Lon"));
        panelForUpperLeft.add(ulonTextBox);
        panelForUpperLeft.add(new Label("Lat"));
        panelForUpperLeft.add(ulatTextBox);
        panelForUpperLeft.setSpacing(5);
        panelForFiltering.add(panelForUpperLeft);
        
        
        panelForFiltering.add(new HTML("Lower Bottom:<br/>"));
        HorizontalPanel panelForLowerBottom=new HorizontalPanel();
        panelForLowerBottom.add(new Label("Lon"));
        panelForLowerBottom.add(llonTextBox);
        panelForLowerBottom.add(new Label("Lat"));
        panelForLowerBottom.add(llatTextBox);
        panelForLowerBottom.setSpacing(5);
        
        panelForFiltering.add(panelForLowerBottom);
        
        panelForFiltering.add(new HTML("<br/>"));
        panelForFiltering.add(spatialFilterButton);
        panelForFiltering.add(clearFilterButton);
        
        panelForFiltering.setSpacing(2);
        
        map.addMapClickListener(new MapClickListener() {
            public void onClick(MapClickListener.MapClickEvent mapClickEvent) {
                
                LonLat lonLat = mapClickEvent.getLonLat();
                lonLat.transform(map.getProjection(), DEFAULT_PROJECTION.getProjectionCode()); //transform lonlat to more readable format
                
                if(filterTabSelected && ulonTextBox.isEnabled()) {
                    ulonTextBox.setText(Double.toString((lonLat.lon())));
                    ulatTextBox.setText(Double.toString((lonLat.lat())));
                    ulonTextBox.setEnabled(false);
                    ulatTextBox.setEnabled(false);
                }
                else if(filterTabSelected && !ulonTextBox.isEnabled()) {
                    llonTextBox.setText(Double.toString((lonLat.lon())));
                    llatTextBox.setText(Double.toString((lonLat.lat())));
                }
            }
        });
        
        spatialFilterButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
                VectorFeature []features=stationsLayer.getFeatures();
                double lat,lon;
                for(int i=0;i<features.length;i++) {
                    String []lonLat=stationsLatLonList.get(i).split(" ");
                    lat=Double.parseDouble(lonLat[0]);
                    lon=Double.parseDouble(lonLat[1]);
                    
                    if((lon>=Double.parseDouble(ulonTextBox.getText())) && (lon<=Double.parseDouble(llonTextBox.getText())) && (lat<=Double.parseDouble(ulatTextBox.getText())) && (lat>=Double.parseDouble(llatTextBox.getText()))) {
                    
                    } 
                    else {
                        stationsLayer.removeFeature(features[i]);
                    } 
                }
            }
        });
        
        clearFilterButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
                //clearing the fields
                ulonTextBox.setEnabled(true);
                ulatTextBox.setEnabled(true);
                ulonTextBox.setText("");
                ulatTextBox.setText("");
                llonTextBox.setText("");
                llatTextBox.setText("");
            }
        });
        
        panelForFiltering.add(temporalLabel);
        Label l1 = new Label("Start date");
        final TextBox date1 = new TextBox();
        date1.setText("YYYY-MM-DD");
        
        Label l2 = new Label("Start time");
        final TextBox time1 = new TextBox();
        time1.setText("HH:mm:ss");
        
        Label l3 = new Label("End date");
        final TextBox date2 = new TextBox();
        date2.setText("YYYY-MM-DD");
        
        Label l4 = new Label("End time");
        final TextBox time2 = new TextBox();
        time2.setText("HH:mm:ss");
        
        temporalFilterButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
                String selectedServer=serverList.getItemText(serverList.getSelectedIndex());
                String url=server.get(selectedServer);
                url+="&request=GetObservation&version=1.0.0&observedProperty=";
                url+=observedPropertyList.getItemText(observedPropertyList.getSelectedIndex());
                
                NodeList children=((Element)offerings.item(offeringsList.getSelectedIndex()+1)).getElementsByTagName("name");
              
                url+="&offering="+children.item(0).getFirstChild().getNodeValue();
                url+="&responseFormat=text%2Fcsv";
                url+="&eventTime="+date1.getText()+"T"+time1.getText()+"Z/"+date2.getText()+"T"+time2.getText()+"Z";
                
                System.out.println(url);
                
                gb.getObservations(url);
            }
        });
        
        panelForFiltering.add(l1);
        panelForFiltering.add(date1);
        panelForFiltering.add(l2);
        panelForFiltering.add(time1);
        panelForFiltering.add(l3);
        panelForFiltering.add(date2);
        panelForFiltering.add(l4);
        panelForFiltering.add(time2);
        panelForFiltering.add(new HTML("<br/>"));
        panelForFiltering.add(temporalFilterButton);
        
        panelForFiltering.add(visualizeLabel);
        panelForFiltering.add(visualizeButton);
        
        visualizeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
                FormPanel form = new FormPanel("_self");
                form.setMethod(FormPanel.METHOD_POST);
                
                NodeList children=((Element)offerings.item(offeringsList.getSelectedIndex()+1)).getElementsByTagName("name");
                
                Hidden params0 = new Hidden("url", server.get(serverList.getItemText(serverList.getSelectedIndex()))+"&request=GetObservation&version=1.0.0&observedProperty="
                        + observedPropertyList.getItemText(observedPropertyList.getSelectedIndex())+"&offering="+
                        children.item(0).getFirstChild().getNodeValue()+"&responseFormat=text%2Fcsv"+"&eventTime="+date1.getText()+"T"+time1.getText()+"Z/"+date2.getText()+"T"+time2.getText()+"Z");
                        
                Hidden params1 = new Hidden("param1", "value2");
                Hidden params2 = new Hidden("param2", "value3");

                FlowPanel panel = new FlowPanel();
                panel.add(params0);
                panel.add(params1);
                panel.add(params2);

                form.add(panel);

                form.setAction("http://192.168.1.110/gnr629/");
                RootPanel.get().add(form);
                form.submit();
            }
        });
        
        return panelForFiltering;
    }
    
    private static VerticalPanel prepareOperationsPanel(RequestBuilder builder,String serverUrl) {

        final VerticalPanel panelForOperations=new VerticalPanel();
        final HTML operationsLabel=new HTML("<p style='border-bottom:1px solid black;'>Supported Operations</p>");
        final HTML offeringsLabel=new HTML("<p style='border-bottom:1px solid black;'>Supported Offerings</p>");
        final VerticalPanel panelForOfferings=new VerticalPanel();
        final HTML offeringsDetailsLabel=new HTML("<p style='border-bottom:1px solid black;'>Offering Details</p>");
        final HTML offeringsObservedProperty=new HTML("<p style='border-bottom:1px solid black;'>Observed Properties</p>");
        final ListBox operationsList=new ListBox();
        final Button submit=new Button("Get Observations");
        
        String requestData=null;
        builder.setHeader("Accept", "text/html,application/xhtml+xml,application/xml");
        try {
            builder.sendRequest(requestData, new RequestCallback() {

                @Override
                public void onResponseReceived(Request request, Response response) {
                    
                    String xmlstr = null;
                    if (200 == response.getStatusCode()) {
                            xmlstr = response.getText();
                            
                            final Document messageDom = XMLParser.parse(xmlstr);
                            
                            final NodeList operations=messageDom.getElementsByTagName("Operation");
                            if(operations.item(0).hasChildNodes())
                            {
                               //List of the operations which are supported
                               for(int i=0;i<operations.getLength();i++)
                               {
                                   operationsList.addItem(operations.item(i).getAttributes().getNamedItem("name").getNodeValue()); 
                               }
                            }
                            
                            //adding the operations list
                            panelForOperations.add(operationsLabel);
                            panelForOperations.add(operationsList);
                            
                            //List of offerings supported by the server
                            offerings=messageDom.getElementsByTagName("ObservationOffering");
                            if(offerings.item(0).hasChildNodes())
                            {
                               for(int i=1;i<offerings.getLength();i++)
                               {
                                   offeringsList.addItem(offerings.item(i).getAttributes().getNamedItem("gml:id").getNodeValue()); 
                                   stationsLatLonList.add(((Element)offerings.item(i)).getElementsByTagName("lowerCorner").item(0).getFirstChild().getNodeValue());
                               }
                            }
                            
                            final NodeList stationsName=messageDom.getElementsByTagName("description");
                            for(int i=1;i<stationsName.getLength();i++) {
                                
                                if(stationsName.item(i).getChildNodes()!=null && stationsName.item(i).getChildNodes().item(0)!=null)
                                    stationNamesList.add(stationsName.item(i).getChildNodes().item(0).getNodeValue());
                                else
                                    stationNamesList.add(offerings.item(i).getAttributes().getNamedItem("gml:id").getNodeValue());
                            }
                            
                            //adding the offerings
                            panelForOperations.add(offeringsLabel);
                            panelForOperations.add(offeringsList);
                            
                            panelForOfferings.add(offeringsDetailsLabel);
                            
                            NodeList children=((Element)offerings.item(1)).getElementsByTagName("name");
                            panelForOfferings.add(new HTML("Offerings Name: "+children.item(0).getFirstChild().getNodeValue()));
                            
                            children=((Element)offerings.item(1)).getElementsByTagName("lowerCorner");
                            panelForOfferings.add(new HTML("<br/>Bounded By: "+children.item(0).getFirstChild().getNodeValue()));
                            
                            children=((Element)offerings.item(1)).getElementsByTagName("observedProperty");
                            panelForOfferings.add(offeringsObservedProperty);
                            for(int i=0;i<children.getLength();i++) {
                               
                                observedPropertyList.addItem(children.item(i).getAttributes().getNamedItem("xlink:href").getNodeValue());
                            }
                            panelForOfferings.add(observedPropertyList);
                            panelForOperations.add(panelForOfferings);
                            
                            offeringsList.addChangeHandler(new ChangeHandler() {
                                
                                @Override
                                public void onChange(ChangeEvent event) {
                                    // TODO Auto-generated method stub
                                    
                                    observedPropertyList.clear();
                                    
                                    for(int i=0;i<panelForOfferings.getWidgetCount();i++) {
                                        panelForOfferings.remove(i);
                                    }
                                    panelForOfferings.add(offeringsDetailsLabel);
                                    
                                    NodeList children=((Element)offerings.item(offeringsList.getSelectedIndex()+1)).getElementsByTagName("name");
                                    panelForOfferings.add(new HTML("Offerings Name: "+children.item(0).getFirstChild().getNodeValue()));
                                    
                                    children=((Element)offerings.item(offeringsList.getSelectedIndex()+1)).getElementsByTagName("lowerCorner");
                                    panelForOfferings.add(new HTML("<br/>Bounded By: "+children.item(0).getFirstChild().getNodeValue()));
                                    
                                    children=((Element)offerings.item(offeringsList.getSelectedIndex()+1)).getElementsByTagName("observedProperty");
                                    panelForOfferings.add(offeringsObservedProperty);
                                    for(int i=0;i<children.getLength();i++) {
                                       
                                        observedPropertyList.addItem(children.item(i).getAttributes().getNamedItem("xlink:href").getNodeValue());
                                    }
                                    panelForOfferings.add(observedPropertyList);
                                    panelForOperations.add(panelForOfferings);
                                }
                            });
                            
                            panelForOperations.add(new HTML("<br/>"));
                            panelForOperations.add(submit);
                            
                            submit.addClickHandler(new ClickHandler() {
                                
                                @Override
                                public void onClick(ClickEvent event) {
                                    // TODO Auto-generated method stub
                                    String selectedServer=serverList.getItemText(serverList.getSelectedIndex());
                                    String url=server.get(selectedServer);
                                    url+="&request=GetObservation&version=1.0.0&observedProperty=";
                                    url+=observedPropertyList.getItemText(observedPropertyList.getSelectedIndex());
                                    
                                    NodeList children=((Element)offerings.item(offeringsList.getSelectedIndex()+1)).getElementsByTagName("name");
                                  
                                    url+="&offering="+children.item(0).getFirstChild().getNodeValue();
                                    url+="&responseFormat=text%2Fcsv";
                                    
                                    System.out.println(url);
                                    
                                    gb.getObservations(url);
                                }
                            });
                            
                            /**
                             * Now will start adding the markers one by one from the stations list
                             */
                            ClusterStrategy clusterStrategy = new ClusterStrategy();
                            clusterStrategy.setDistance(20);
                            clusterStrategy.setThreshold(2);
                            VectorOptions vectorOptions = new VectorOptions();
                            vectorOptions.setStrategies(new Strategy[]{clusterStrategy});
                            stationsLayer = new Vector("Stations",vectorOptions);
                            VectorFeature[] point_features = new VectorFeature[stationsLatLonList.size()];
                            
                            /*
                             * Setting the styles for the marker
                             * */
                            Style st=new Style();
                            st.setGraphicSize(7, 14);
                            st.setExternalGraphic("http://www.clker.com/cliparts/R/K/r/C/f/o/red-marker-black-border-md.png");
                            st.setFillOpacity(0.8);
                            
                            StyleMap stMap=new StyleMap(st);
                            stationsLayer.setStyleMap(stMap);
                            
                            for(int i=0;i<stationsLatLonList.size();i++) {
                                
                                String []latlon=stationsLatLonList.get(i).split(" ");
                                LonLat lonLat = new LonLat(Double.parseDouble(latlon[1]),Double.parseDouble(latlon[0]));
                                lonLat.transform(DEFAULT_PROJECTION.getProjectionCode(),map.getProjection()); 
                                
                                Point p=new Point(lonLat.lon(),lonLat.lat());
                                point_features[i]=new VectorFeature(p);
                                
                                Attributes atd=new Attributes();
                                atd.setAttribute("name",stationNamesList.get(i)+"\n"+stationsLatLonList.get(i));
                                point_features[i].setAttributes(atd);
                                stationsLayer.addFeature(point_features[i]);
                            }
                            
                            
                            //clusterStrategy.setFeatures(point_features);
                            map.addLayer(stationsLayer);
                            
                            SelectFeature selectFeature = new SelectFeature(stationsLayer);
                            selectFeature.setAutoActivate(true);
                            map.addControl(selectFeature);
                     
                            // Secondly add a VectorFeatureSelectedListener to the feature
                            stationsLayer.addVectorFeatureSelectedListener(new VectorFeatureSelectedListener()
                            {
                                public void onFeatureSelected(FeatureSelectedEvent eventObject)
                                {
                                    // Attach a popup to the point, we use null as size cause we set
                                    // autoSize to true
                                    // Note that we use FramedCloud... This extends a normal popup
                                    // and creates is styled as a balloon
                                    
                                    //VectorFeature[] clusters = eventObject.getVectorFeature().getCluster();
                     
                                    Popup popup = new FramedCloud("id1", eventObject.getVectorFeature().getCenterLonLat(), null,"Sensor Location:"+eventObject.getVectorFeature().getAttributes().getAttributeAsString("name"), null, true);
                                    popup.setPanMapIfOutOfView(true); // this set the popup in a
                                                                      // strategic way, and pans the
                                                                      // map if needed.
                                    popup.setAutoSize(true);
                                    eventObject.getVectorFeature().setPopup(popup);
                     
                                    // And attach the popup to the map
                                    map.addPopup(eventObject.getVectorFeature().getPopup());
                                }
                            });
                            
                    }
                    else {
                        Window.alert("Could not connect to server Error:"+response.getStatusCode());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    
                    
                }
                
            });
        }catch(RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return panelForOperations;
    }

    private static MapOptions setMapOtpions(String projection)
    {
        MapOptions mapOptions = new MapOptions();
        mapOptions.setControls(new JObjectArray(new JSObject[] {}));
        mapOptions.setNumZoomLevels(10);
        mapOptions.setProjection(projection);

        return mapOptions;
    }

    private static GoogleV3 getGoogleMapLayer()
    {
        GoogleV3Options gNormalOptions = new GoogleV3Options();
        gNormalOptions.setIsBaseLayer(true);
        gNormalOptions.setType(GoogleV3MapType.G_NORMAL_MAP);
        GoogleV3 gNormal = new GoogleV3("Google Normal", gNormalOptions);
        return gNormal;
    }

    private static void setMap() {
        map = mapWidget.getMap();
        // adding google Base Map layer on map
        GoogleV3 googleMapLayer=getGoogleMapLayer();
        //adding the layer to the map
        map.addLayer(googleMapLayer);
        googleMapLayer.setIsVisible(true);
        
        /** */
        LonLat lonLat = new LonLat(-100.99, 40.73);
        lonLat.transform(DEFAULT_PROJECTION.getProjectionCode(),map.getProjection()); 
        map.setCenter(lonLat, 7);
        
        //adding various controls
        map.addControl(new LayerSwitcher()); 
        map.addControl(new OverviewMap()); 
        map.addControl(new ScaleLine()); 
        map.addControl(new MouseDefaults());
    }
}