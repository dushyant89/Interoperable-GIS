
package edu.iitb.csre.client;

import java.util.HashMap;

import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MouseDefaults;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.control.WMSGetFeatureInfo;
import org.gwtopenmaps.openlayers.client.control.WMSGetFeatureInfoOptions;
import org.gwtopenmaps.openlayers.client.event.GetFeatureInfoListener;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3MapType;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3Options;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.util.JObjectArray;
import org.gwtopenmaps.openlayers.client.util.JSObject;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GNR629Project_1 implements EntryPoint {
    private static final Projection DEFAULT_PROJECTION = new Projection("EPSG:4326");

    public static MapWidget mapWidget;
    public static Map map;
    public static WMS wmsLayer;
    public static HashMap<String,String> server=new HashMap<String,String>();
    public static ListBox serverList;
    public static LayoutPanel panelForMap = new LayoutPanel();
    public static ScrollPanel scrollPanel=new ScrollPanel();
    private static TabPanel servicePanel = new TabPanel();
    private static VerticalPanel panelForWMS;
    private static VerticalPanel panelForWCS;
    private static VerticalPanel panelForWFS;
    private static String referenceTagName="CRS";
    private WFSManager wfsManager;
    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        
        wfsManager = new WFSManager();
        
        //adding the serverList
        server.put("Geoserver","http://localhost:8083/geoserver/");
        server.put("NASA Socio Economic Data","http://sedac.ciesin.columbia.edu/geoserver/gwc/service/");
        
        // let’s create map widget and map objects
        mapWidget = new MapWidget("100%","623px", setMapOtpions("EPSG:4326"));
        map = mapWidget.getMap();
        // adding google Base Map layer on map
        GoogleV3 googleMapLayer=getGoogleMapLayer();
        
        map.addLayer(googleMapLayer);
        googleMapLayer.setIsVisible(true);
        LonLat lonLat = new LonLat(-100.99, 40.73);
        lonLat.transform(DEFAULT_PROJECTION.getProjectionCode(),map.getProjection()); 
        map.setCenter(lonLat, 7);
        
        map.addControl(new LayerSwitcher()); 
        map.addControl(new OverviewMap()); 
        map.addControl(new ScaleLine()); 
        map.addControl(new MouseDefaults());
        
        map.addLayer(wfsManager.getWFSLayer());
        
        //adding layer to the dock panel
        panelForMap.add(mapWidget);
        panelForMap.setWidth("100%");
        panelForMap.setHeight("100%");
        
        //Getting the getCapabilities content for WMS for a default server 
        RequestBuilder builderForWMS=new RequestBuilder(RequestBuilder.GET, "http://localhost:8083/geoserver/wms?request=getCapabilities");
        
        //Getting the getCapabilities content for WCS for a default server
        RequestBuilder builderForWCS=new RequestBuilder(RequestBuilder.GET, "http://localhost:8083/geoserver/wcs?request=GetCapabilities");
        
        panelForWMS=prepareWMSPanel(builderForWMS,"Geoserver");
        panelForWCS=prepareWCSPanel(builderForWCS, "Geoserver");
        panelForWFS=wfsManager.getWFSPanel();
        
        VerticalPanel panelForConfiguration=changeConfigurationPanel();
        
        scrollPanel.setVerticalScrollPosition(2);
        //here the code for get capabilities will come
        servicePanel.add(panelForConfiguration,"Configure");
        servicePanel.add(panelForWMS, "WMS");
        servicePanel.add(panelForWFS, "WFS");
        servicePanel.add(panelForWCS, "WCS");
        
        // Show the 'bar' tab initially.
        servicePanel.selectTab(1);
        servicePanel.setAnimationEnabled(true);
        
        scrollPanel.add(servicePanel);
        
        Label title=new Label("GNR 629 Project");
        title.setStyleName("gwt-title");
        panelForMap.add(title);
        panelForMap.setWidgetVerticalPosition(title,Layout.Alignment.BEGIN);
        panelForMap.setWidgetLeftWidth(title, 43, Unit.PCT, 150, Unit.PX);
        panelForMap.setWidgetTopHeight(title, 3, Unit.PCT, 50, Unit.PX);
        
        panelForMap.add(scrollPanel);
        
        panelForMap.setWidgetVerticalPosition(scrollPanel,Layout.Alignment.BEGIN);
        panelForMap.setWidgetLeftWidth(scrollPanel, 0, Unit.PCT, 30, Unit.PCT);
        panelForMap.setWidgetTopHeight(scrollPanel, 20, Unit.PCT, 100, Unit.PCT);
        RootLayoutPanel rp= RootLayoutPanel.get();
        rp.add(panelForMap);

    }
    
    private VerticalPanel changeConfigurationPanel() {
        
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
                RequestBuilder builder=new RequestBuilder(RequestBuilder.GET, server.get(selectedServer)+"wms?request=GetCapabilities");
                servicePanel.remove(panelForWMS);
                panelForWMS=prepareWMSPanel(builder,selectedServer);
                servicePanel.add(panelForWMS, "WMS");
                
                builder=new RequestBuilder(RequestBuilder.GET, server.get(selectedServer)+"wcs?request=GetCapabilities");
                servicePanel.remove(panelForWCS);
                panelForWCS=prepareWCSPanel(builder,selectedServer);
                servicePanel.add(panelForWCS, "WCS");
                
                servicePanel.remove(panelForWFS);
                panelForWFS=wfsManager.getWFSPanel();
                servicePanel.add(panelForWFS, "WFS");
            }
        });
        
        configPanel.add(serverList);
        configPanel.add(new HTML("<br/><br/>"));
        configPanel.add(changeServerButton);
        
        return configPanel;
    }
    
    private static VerticalPanel prepareWCSPanel(RequestBuilder builder,final String serverUrl)
    {
        String requestData = null;
        final ListBox operationList=new ListBox();
        final ListBox formatsList=new ListBox();
        final ListBox coverageList= new ListBox();
        final VerticalPanel panelForWCS=new VerticalPanel();
        final HorizontalPanel subtypeLabel=new HorizontalPanel();
        final HorizontalPanel boundingBoxLabel=new HorizontalPanel();
        final Button describeCoverageButton=new Button("Describe");
        final Label getCoverageLabel=new Label("Use Describe Coverage for this request");
        final Label supportedCoverageLabel=new Label("Supported Coverages:");
        final Label supportedFormatLabel=new Label("Supported Request Formats:");
        final HTML describeCoverageLabel=new HTML("<p style='border-bottom:1px solid black;'>Describe Coverage</p>");
        final HTML metadataLabel=new HTML("<p style='border-bottom:1px solid black;'>Service Level  Metadata</p>");
        final HTML coverageDetailsLabel=new HTML("<p style='border-bottom:1px solid black;'>Details for the Coverage</p>");
        
        
        try {
            builder.sendRequest(requestData, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                // TODO Auto-generated method stub
                    String xmlstr = null;
                    if (200 == response.getStatusCode()) {
                            xmlstr = response.getText();
                            
                            final Document messageDom = XMLParser.parse(xmlstr);
                            final NodeList operation=messageDom.getElementsByTagName("Operation");
                            
                            for(int i=0;i<operation.getLength();i++)
                            {
                                NamedNodeMap names=operation.item(i).getAttributes(); 
                                operationList.addItem(names.getNamedItem("name").getNodeValue());
                            }
                            
                            final NodeList format=messageDom.getElementsByTagName("formatSupported");
                            
                            for(int i=0;i<format.getLength();i++)
                            {
                                formatsList.addItem(format.item(i).getFirstChild().getNodeValue());
                            }
                            
                            final NodeList coverageSummary=messageDom.getElementsByTagName("CoverageSummary");
                            
                            for(int i=0;i<coverageSummary.getLength();i++)
                            {
                               coverageList.addItem(((Element)coverageSummary.item(i)).
                                                    getElementsByTagName("CoverageId").item(0).getFirstChild().getNodeValue()); 
                            }
                            
                            operationList.addChangeHandler(new ChangeHandler() {
                                
                                @Override
                                public void onChange(ChangeEvent event) {
                                    // TODO Auto-generated method stub
                                    if(operationList.getItemText(operationList.getSelectedIndex()).equalsIgnoreCase("DescribeCoverage"))
                                    {
                                      //removing other components first
                                        if(panelForWCS.remove(coverageDetailsLabel))
                                        {
                                            for(int i=0;i<subtypeLabel.getWidgetCount();i++)
                                            {    
                                                subtypeLabel.remove(i);
                                            }
                                            for(int i=0;i<boundingBoxLabel.getWidgetCount();i++)
                                            {    
                                                boundingBoxLabel.remove(i);
                                            }
                                            panelForWCS.remove(subtypeLabel);
                                            panelForWCS.remove(boundingBoxLabel);
                                        }
                                        panelForWCS.remove(coverageList);
                                        panelForWCS.remove(formatsList);
                                        panelForWCS.remove(supportedFormatLabel);
                                        panelForWCS.remove(supportedCoverageLabel);
                                        panelForWCS.remove(metadataLabel);
                                        panelForWCS.remove(getCoverageLabel);
                                        panelForWCS.add(describeCoverageLabel);
                                        
                                        panelForWCS.add(describeCoverageButton);
                                        describeCoverageButton.addClickHandler(new ClickHandler() {
                                            
                                            @Override
                                            public void onClick(ClickEvent event) {
                                                // TODO Auto-generated method stub
                                                String describeCoverageRequest=server.get(serverList.getItemText(serverList.getSelectedIndex()));
                                                describeCoverageRequest+="wcs?request=DescribeCoverage&version=1.0.0";
                                                RequestBuilder dc=new RequestBuilder(RequestBuilder.GET,describeCoverageRequest);
                                                try{
                                                    
                                                    dc.sendRequest(null,new RequestCallback() {
                                                        @Override
                                                        public void onResponseReceived(Request request, Response response) {
                                                             
                                                            DialogBox describeCoverage_db=new DialogBox(true,true);
                                                            describeCoverage_db.show();
                                                            describeCoverage_db.center();
                                                            String html="<head><title>Geoserver GetFeatureInfo output</title></head><style type=";
                                                            html+="'text/css'>table.describeCoverage, table.describeCoverage td, table.describeCoverage th {";
                                                            html+="border:1px solid #ddd;border-collapse:collapse;margin:0";
                                                            html+="padding:0;font-size: 90%;padding:.2em .1em;}";
                                                            html+="table.describeCoverage th {padding:.2em .2em;font-weight:bold;background:#eee;}";
                                                            html+="table.describeCoverage td{background:#fff;}";
                                                            html+="table.describeCoverage tr.odd td{background:#eee;}table.describeCoverage caption{text-align:left";
                                                            html+="font-size:100%;font-weight:bold;padding:.2em .2em;}</style><body>";
                                                            
                                                            html+="<table class='describeCoverage'><caption class='describeCoverage'>Describe Coverage</caption>";
                                                            html+="<tr> <th>Name </th> <th> Supported CRSs</th> <th>Formats </th> <th> Supported Interpolation</th><th> Action </th> </tr>";
                                                                    
                                                            final Document message = XMLParser.parse(response.getText());
                                                            final NodeList coverages=message.getElementsByTagName("CoverageOffering");
                                                            
                                                            for(int i=0;i<coverages.getLength();i++)
                                                            {    
                                                                if(i%2==0)
                                                                    html+="<tr>";
                                                                else
                                                                    html+="<tr class='odd'>";
                                                                
                                                                String targetUrl=server.get(serverList.getItemText(serverList.getSelectedIndex()))+"wcs";
                                                                targetUrl+="?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage";
                                                                
                                                                Node coverage=coverages.item(i);
                                                                html+="<td>"+((Element)coverage).getElementsByTagName("name").item(0).getFirstChild().getNodeValue()+"</td>";
                                                                
                                                                targetUrl+="&COVERAGE="+((Element)coverage).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                                                                
                                                                html+="<td>";
                                                                Node supportedCRS=((Element)coverage).getElementsByTagName("supportedCRSs").item(0);
                                                                NodeList crss=((Element)supportedCRS).getElementsByTagName("requestResponseCRSs");
                                                                
                                                                targetUrl+="&CRS="+crss.item(0).getFirstChild().getNodeValue();
                                                                
                                                                for(int j=0;j<crss.getLength();j++)
                                                                {    
                                                                     if(j!=crss.getLength()-1)   
                                                                        html+=crss.item(j).getFirstChild().getNodeValue()+",";
                                                                     else
                                                                         html+=crss.item(j).getFirstChild().getNodeValue(); 
                                                                }
                                                                html+="</td>";
                                                                
                                                                Node latlon=((Element)coverage).getElementsByTagName("lonLatEnvelope").item(0);
                                                                NodeList pos=((Element)latlon).getElementsByTagName("pos");
                                                                String []posValues=pos.item(0).getFirstChild().getNodeValue().split(" ");
                                                                
                                                                targetUrl+="&BBOX="+posValues[0]+","+posValues[1]+",";
                                                                posValues=pos.item(1).getFirstChild().getNodeValue().split(" ");
                                                                targetUrl+=posValues[0]+","+posValues[1]+"&RESX=1&RESY=1&";
                                                                
                                                                
                                                                html+="<td>";
                                                                Node supportedFormats=((Element)coverage).getElementsByTagName("supportedFormats").item(0);
                                                                NodeList formats=((Element)supportedFormats).getElementsByTagName("formats");
                                                                
                                                                targetUrl+="FORMAT="+formats.item(0).getFirstChild().getNodeValue();
                                                                
                                                                for(int j=0;j<formats.getLength();j++)
                                                                {    
                                                                     if(j!=formats.getLength()-1)   
                                                                        html+=formats.item(j).getFirstChild().getNodeValue()+",";
                                                                     else
                                                                         html+=formats.item(j).getFirstChild().getNodeValue(); 
                                                                }
                                                                html+="</td>";
                                                                
                                                                html+="<td>";
                                                                Node supportedInterpolations=((Element)coverage).getElementsByTagName("supportedInterpolations").item(0);
                                                                NodeList interpolationMethods=((Element)supportedInterpolations).getElementsByTagName("interpolationMethod");
                                                                
                                                                for(int j=0;j<interpolationMethods.getLength();j++)
                                                                {    
                                                                     if(j!=interpolationMethods.getLength()-1)   
                                                                        html+=interpolationMethods.item(j).getFirstChild().getNodeValue()+",";
                                                                     else
                                                                        html+=interpolationMethods.item(j).getFirstChild().getNodeValue();
                                                                }
                                                                html+="</td>";
                                                                
                                                                html+="<td><a href='"+targetUrl+"' target='_blank'> Get Coverage </a> </td>";
                                                                
                                                                html+="</tr>";
                                                            }
                                                            
                                                            html+="<table></br></body></html>";
                                                            
                                                            describeCoverage_db.setWidget(new HTML(html));
                                                        }
                                                        
                                                        @Override
                                                        public void onError(Request request, Throwable exception) {
                                                            Window.alert("Exception :"+exception.getMessage());
                                                        }
                                                    });
                                                    
                                                }catch(RequestException e)
                                                {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                        
                                    }else if(operationList.getItemText(operationList.getSelectedIndex()).equalsIgnoreCase("GetCoverage"))
                                    {
                                        
                                        //removing other components first
                                        panelForWCS.remove(describeCoverageLabel);
                                        panelForWCS.remove(describeCoverageButton);
                                        panelForWCS.remove(formatsList);
                                        panelForWCS.remove(supportedFormatLabel);
                                        panelForWCS.remove(supportedCoverageLabel);
                                        panelForWCS.remove(coverageList);
                                        panelForWCS.remove(metadataLabel);
                                        
                                        if(panelForWCS.remove(coverageDetailsLabel))
                                        {
                                            for(int i=0;i<subtypeLabel.getWidgetCount();i++)
                                            {    
                                                subtypeLabel.remove(i);
                                            }
                                            for(int i=0;i<boundingBoxLabel.getWidgetCount();i++)
                                            {    
                                                boundingBoxLabel.remove(i);
                                            }
                                            panelForWCS.remove(subtypeLabel);
                                            panelForWCS.remove(boundingBoxLabel);
                                            
                                            //adding components
                                            panelForWCS.add(getCoverageLabel);
                                        }
                                        
                                    }
                                    else if(operationList.getItemText(operationList.getSelectedIndex()).equalsIgnoreCase("GetCapabilities"))
                                    {
                                        //removing other components first
                                        panelForWCS.remove(describeCoverageLabel);
                                        panelForWCS.remove(describeCoverageButton);
                                        panelForWCS.remove(getCoverageLabel);
                                        
                                        //adding back components
                                        panelForWCS.add(metadataLabel);
                                        panelForWCS.add(supportedFormatLabel);
                                        panelForWCS.add(formatsList);
                                        panelForWCS.add(supportedCoverageLabel);
                                        panelForWCS.add(coverageList);
                                        
                                        panelForWCS.add(coverageDetailsLabel);
                                        
                                        subtypeLabel.add(new Label("Coverage Sub Type: "+
                                            ((Element)coverageSummary.item(0)).getElementsByTagName("CoverageSubtype").
                                            item(0).getFirstChild().getNodeValue()));
                                        
                                        NodeList lowerCorner=((Element)coverageSummary.item(0)).getElementsByTagName("LowerCorner");
                                        NodeList upperCorner=((Element)coverageSummary.item(0)).getElementsByTagName("UpperCorner");
                                        
                                        boundingBoxLabel.add(new Label("Coverage Bounding Box Coordinates: "
                                                +lowerCorner.item(0).getFirstChild().getNodeValue()+
                                                " "+upperCorner.item(0).getFirstChild().getNodeValue()));
                                        
                                        panelForWCS.add(subtypeLabel);
                                        panelForWCS.add(boundingBoxLabel);
                                    }
                                    
                                }
                            });
                            
                            panelForWCS.add(new Label("Requests List:"));
                            panelForWCS.add(operationList);
                            coverageList.addChangeHandler(new ChangeHandler(){
                                @Override
                                public void onChange(ChangeEvent event) {
                                    // TODO Auto-generated method stub
                                    String selectedCoverage=coverageList.getItemText(coverageList.getSelectedIndex());
                                    if(panelForWCS.remove(coverageDetailsLabel))
                                    {
                                        for(int i=0;i<subtypeLabel.getWidgetCount();i++)
                                        {    
                                            subtypeLabel.remove(i);
                                        }
                                        for(int i=0;i<boundingBoxLabel.getWidgetCount();i++)
                                        {    
                                            boundingBoxLabel.remove(i);
                                        }
                                        panelForWCS.remove(subtypeLabel);
                                        panelForWCS.remove(boundingBoxLabel);
                                    }
                                    for(int i=0;i<coverageSummary.getLength();i++)
                                    {
                                       String coverageName=((Element)coverageSummary.item(i)).
                                               getElementsByTagName("CoverageId").item(0).getFirstChild().getNodeValue();
                                       if(coverageName.equals(selectedCoverage))
                                       {   
                                           panelForWCS.add(coverageDetailsLabel);
                                           subtypeLabel.add(new Label("Coverage Sub Type: "+
                                               ((Element)coverageSummary.item(i)).getElementsByTagName("CoverageSubtype").
                                               item(0).getFirstChild().getNodeValue()));
                                           
                                           NodeList lowerCorner=((Element)coverageSummary.item(i)).getElementsByTagName("LowerCorner");
                                           NodeList upperCorner=((Element)coverageSummary.item(i)).getElementsByTagName("UpperCorner");
                                           
                                           boundingBoxLabel.add(new Label("Coverage Bounding Box Coordinates: "
                                                   +lowerCorner.item(0).getFirstChild().getNodeValue()+
                                                   " "+upperCorner.item(0).getFirstChild().getNodeValue()));
                                           
                                           panelForWCS.add(subtypeLabel);
                                           panelForWCS.add(boundingBoxLabel);
                                           
                                           break;
                                       }
                                    }
                                }
                            });
                            panelForWCS.setSpacing(2);
                            panelForWCS.add(metadataLabel);
                            panelForWCS.add(supportedFormatLabel);
                            panelForWCS.add(formatsList);
                            panelForWCS.add(supportedCoverageLabel);
                            panelForWCS.add(coverageList);
                            
                            panelForWCS.add(coverageDetailsLabel);
                            
                            subtypeLabel.add(new Label("Coverage Sub Type: "+
                                ((Element)coverageSummary.item(0)).getElementsByTagName("CoverageSubtype").
                                item(0).getFirstChild().getNodeValue()));
                            
                            NodeList lowerCorner=((Element)coverageSummary.item(0)).getElementsByTagName("LowerCorner");
                            NodeList upperCorner=((Element)coverageSummary.item(0)).getElementsByTagName("UpperCorner");
                            
                            boundingBoxLabel.add(new Label("Coverage Bounding Box Coordinates: "
                                    +lowerCorner.item(0).getFirstChild().getNodeValue()+
                                    " "+upperCorner.item(0).getFirstChild().getNodeValue()));
                            
                            panelForWCS.add(subtypeLabel);
                            panelForWCS.add(boundingBoxLabel);

                            
                        } else {
                            Window.alert("Could not connect to server Error:"+response.getStatusCode());
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        // TODO Auto-generated method stub
                        Window.alert("Oh fax:"+exception.getMessage());
                    }
                });
            }
        catch(RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       return panelForWCS;
    }
    
    private static VerticalPanel prepareWMSPanel(RequestBuilder builder,final String serverUrl)
    {
        String requestData = null;
        final VerticalPanel panelForWMS=new VerticalPanel();
        final ListBox layerList=new ListBox();
        final ListBox featureInfoLayerList=new ListBox();
        final ListBox requestList=new ListBox();
        final ListBox crsList=new ListBox();
        final ListBox stylesList=new ListBox();
        final ListBox formatList=new ListBox();
        final HorizontalPanel textBoxes=new HorizontalPanel();
        final HorizontalPanel boundingboxLabels=new HorizontalPanel();
        final Label layersLabel=new Label("Layers :");
        final Label supportedLayerLabel=new Label("Supported Layers for GetFeatureInfo:");
        final Label stylesLabel=new Label("Styles :");
        final Label crsLabel=new Label(referenceTagName+" :");
        final Label minxLabel=new Label("Minx");
        final Label minyLabel=new Label("Miny");
        final Label maxxLabel=new Label("Maxx");
        final Label maxyLabel=new Label("Maxy");
        final TextBox minX=new TextBox();
        final TextBox minY=new TextBox();
        final TextBox maxX=new TextBox();
        final TextBox maxY=new TextBox();
        final HTML detailsLabel=new HTML("<p style='border-bottom:1px solid black;'>Details for the layer</p>");
        final HTML metadataLabel=new HTML("<p style='border-bottom:1px solid black;'>Service Level  Metadata</p>");
        
        final Button submitButton=new Button("Submit Request");
        
        try {
            builder.sendRequest(requestData, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                // TODO Auto-generated method stub
                    String xmlstr = null;
                    if (200 == response.getStatusCode()) {
                            xmlstr = response.getText();
                            
                            final Document messageDom = XMLParser.parse(xmlstr);
                            
                            //prepare the list of requests which are supported
                            final NodeList requests=messageDom.getElementsByTagName("Request");
                            if(requests.item(0).hasChildNodes())
                            {
                               NodeList requestNames=requests.item(0).getChildNodes();
                               for(int i=0;i<requestNames.getLength();i++)
                               {
                                    if(!requestNames.item(i).getNodeName().equalsIgnoreCase("#text"))
                                    {   
                                       requestList.addItem(requestNames.item(i).getNodeName()); 
                                    }
                               }
                            }
                            
                            //adding the change handlers for request lists
                            requestList.addChangeHandler(new ChangeHandler(){
                                @Override
                                public void onChange(ChangeEvent event) {
                                    
                                    String requestName=requestList.getValue(requestList.getSelectedIndex());
                                    NodeList requestNames=requests.item(0).getChildNodes();
                                    int i;
                                    for(i=0;i<requestNames.getLength();i++)
                                    {
                                         if(requestNames.item(i).getNodeName().equalsIgnoreCase(requestName))
                                         {   
                                            break;
                                         }
                                    }
                                    
                                    formatList.clear();
                                    NodeList formats=((Element)requests.item(0).getChildNodes().item(i)).getElementsByTagName("Format");
                                    for(i=0;i<formats.getLength();i++)
                                    {
                                        formatList.addItem(formats.item(i).getFirstChild().getNodeValue());
                                    }
                                    
                                    if(requestName.equals("GetMap"))
                                    {   
                                        
                                        if(panelForWMS.remove(supportedLayerLabel))
                                        {
                                           panelForWMS.remove(featureInfoLayerList); 
                                        }
                                        
                                        //prepare the list of supported layers
                                        final NodeList layers = messageDom.getElementsByTagName("Layer");
                                        int num = layers.getLength();
                                        for(i=1; i< num ; i++){
                                        Node name = ((Element)layers.item(i)).getElementsByTagName("Name").item(0);
                                        String strname= name.getFirstChild().getNodeValue();
                                            layerList.addItem(strname);
                                        }
                                        
                                        //adding change handler for the list of layers
                                        layerList.addChangeHandler(new ChangeHandler(){

                                            @Override
                                            public void onChange(ChangeEvent event) {
                                                // TODO Auto-generated method stub
                                                
                                                NodeList crs=((Element)layers.item(layerList.getSelectedIndex()+1)).getElementsByTagName(referenceTagName);
                                                crsList.clear();
                                                for(int i=0;i<crs.getLength();i++)
                                                {
                                                    crsList.addItem(crs.item(i).getFirstChild().getNodeValue());
                                                }
                                                NodeList styles=((Element)layers.item(layerList.getSelectedIndex()+1)).getElementsByTagName("Style");
                                                stylesList.clear();
                                                if(styles.getLength()>0)
                                                {
                                                    for(int i=0;i<styles.getLength();i++)
                                                    {
                                                        Node name = ((Element)styles.item(i)).getElementsByTagName("Name").item(0);
                                                        stylesList.addItem(name.getFirstChild().getNodeValue());
                                                    }
                                                }
                                                else
                                                {
                                                    stylesList.addItem("NA"); 
                                                }
                                            }
                                            
                                        });
                                        
                                        //preparing the CRS list
                                        NodeList crs=((Element)layers.item(1)).getElementsByTagName(referenceTagName);
                                        if(crs.getLength()==0 && referenceTagName.equals("CRS"))
                                        {
                                            referenceTagName="SRS";
                                            crs=((Element)layers.item(1)).getElementsByTagName(referenceTagName);
                                        }
                                        else if(crs.getLength()==0 && referenceTagName.equals("SRS"))
                                        {    
                                            referenceTagName="CRS";
                                            crs=((Element)layers.item(1)).getElementsByTagName(referenceTagName);
                                        }
                                        for(i=0;i<crs.getLength();i++)
                                        {
                                            crsList.addItem(crs.item(i).getFirstChild().getNodeValue());
                                        }
                                        
                                        crsList.addChangeHandler(new ChangeHandler() {
                                            
                                            @Override
                                            public void onChange(ChangeEvent event) {
                                               //updating the text boxes
                                                String crs=crsList.getValue(crsList.getSelectedIndex());
                                                NodeList boundingBox=((Element)layers.item(layerList.getSelectedIndex()+1)).getElementsByTagName("BoundingBox");
                                                int i;
                                                for(i=0;i<boundingBox.getLength();i++)
                                                {
                                                    if(((Element)boundingBox.item(i)).getAttributes().getNamedItem(referenceTagName).getNodeValue().equals(crs))
                                                    {
                                                        break;
                                                    }
                                                }
                                                textBoxes.clear();
                                                minX.setWidth("40px");
                                                minX.setHeight("10px");
                                                minX.setValue(((Element)boundingBox.item(i)).getAttributes().getNamedItem("minx").getNodeValue());
                                                minY.setWidth("40px");
                                                minY.setHeight("10px");
                                                minY.setValue(((Element)boundingBox.item(i)).getAttributes().getNamedItem("miny").getNodeValue());
                                                maxX.setWidth("40px");
                                                maxX.setHeight("10px");
                                                maxX.setValue(((Element)boundingBox.item(i)).getAttributes().getNamedItem("maxx").getNodeValue());
                                                maxY.setWidth("40px");
                                                maxY.setHeight("10px");
                                                maxY.setValue(((Element)boundingBox.item(i)).getAttributes().getNamedItem("maxy").getNodeValue());
                                                textBoxes.add(minX);textBoxes.add(minY);textBoxes.add(maxX);textBoxes.add(maxY);
                                                textBoxes.setSpacing(10);
                                            }
                                        });
                                        
                                        //preparing the styles list
                                        NodeList styles=((Element)layers.item(1)).getElementsByTagName("Style");
                                        if(styles.getLength()>0)
                                        {
                                            for(i=0;i<styles.getLength();i++)
                                            {
                                                Node name = ((Element)styles.item(i)).getElementsByTagName("Name").item(0);
                                                stylesList.addItem(name.getFirstChild().getNodeValue());
                                            }
                                        }
                                        else
                                        {
                                            stylesList.addItem("NA");
                                        }
                                        panelForWMS.add(layersLabel);
                                        panelForWMS.add(layerList);
                                        panelForWMS.add(detailsLabel);
                                        panelForWMS.add(crsLabel);
                                        panelForWMS.add(crsList);
                                        panelForWMS.add(stylesLabel);
                                        panelForWMS.add(stylesList);
                                        
                                        boundingboxLabels.add(minxLabel);
                                        boundingboxLabels.add(minyLabel);
                                        boundingboxLabels.add(maxxLabel);
                                        boundingboxLabels.add(maxyLabel);
                                        boundingboxLabels.setSpacing(10);
                                        
                                        panelForWMS.add(boundingboxLabels);
                                        
                                      //preparing the textboxes for bounding boxes
                                        NodeList boundingBox=((Element)layers.item(1)).getElementsByTagName("BoundingBox");
                                        minX.setWidth("40px");
                                        minX.setHeight("10px");
                                        minX.setValue(((Element)boundingBox.item(0)).getAttributes().getNamedItem("minx").getNodeValue());
                                        minY.setWidth("40px");
                                        minY.setHeight("10px");
                                        minY.setValue(((Element)boundingBox.item(0)).getAttributes().getNamedItem("miny").getNodeValue());
                                        maxX.setWidth("40px");
                                        maxX.setHeight("10px");
                                        maxX.setValue(((Element)boundingBox.item(0)).getAttributes().getNamedItem("maxx").getNodeValue());
                                        maxY.setWidth("40px");
                                        maxY.setHeight("10px");
                                        maxY.setValue(((Element)boundingBox.item(0)).getAttributes().getNamedItem("maxy").getNodeValue());
                                        textBoxes.add(minX);textBoxes.add(minY);textBoxes.add(maxX);textBoxes.add(maxY);
                                        textBoxes.setSpacing(10);
                                        panelForWMS.add(textBoxes);
                                        panelForWMS.setSpacing(2);
                                        panelForWMS.add(submitButton);
                                        
                                        //get the map on click of submit button
                                        submitButton.addClickHandler(new ClickHandler() {
                                            @Override
                                            public void onClick(ClickEvent event) {
                                                String layerName=layerList.getItemText(layerList.getSelectedIndex());
                                                String requestUrl=server.get(serverUrl)+"wms";
                                                String styleName=stylesList.getItemText(stylesList.getSelectedIndex());
                                                
                                                WMSParams wmsParams = new WMSParams();
                                                wmsParams.setLayers(layerName);
                                                wmsParams.setFormat("image/png");
                                                wmsParams.setIsTransparent(true);
                                                if(!styleName.equals("NA"))
                                                {
                                                    wmsParams.setStyles(styleName);
                                                }
                                                
                                                WMSOptions wmsLayerParams = new WMSOptions();
                                                wmsLayerParams.setDisplayOutsideMaxExtent(true);
                                                wmsLayerParams.setProjection(crsList.getItemText(crsList.getSelectedIndex()));
                                                wmsLayerParams.setLayerOpacity(1);
                                                wmsLayer=new WMS(layerName, requestUrl, wmsParams,wmsLayerParams);
                                                wmsLayer.setIsBaseLayer(false); 
                                                
                                                map.addLayer(wmsLayer);
                                                
                                                //Adds the WMSGetFeatureInfo control
                                                WMSGetFeatureInfoOptions wmsGetFeatureInfoOptions = new WMSGetFeatureInfoOptions();
                                                wmsGetFeatureInfoOptions.setMaxFeaturess(50);
                                                wmsGetFeatureInfoOptions.setLayers(new WMS[]{wmsLayer});
                                                wmsGetFeatureInfoOptions.setDrillDown(true);
                                                //to request a GML string instead of HTML : wmsGetFeatureInfoOptions.setInfoFormat(GetFeatureInfoFormat.GML.toString());
                                         
                                                WMSGetFeatureInfo wmsGetFeatureInfo = new WMSGetFeatureInfo(
                                                        wmsGetFeatureInfoOptions);
                                         
                                                wmsGetFeatureInfo.addGetFeatureListener(new GetFeatureInfoListener() {
                                                    public void onGetFeatureInfo(GetFeatureInfoEvent eventObject) {
                                                        //if you did a wmsGetFeatureInfoOptions.setInfoFormat(GetFeatureInfoFormat.GML.toString()) you can do a VectorFeature[] features = eventObject.getFeatures(); here
                                                        DialogBox db2 = new DialogBox(true,true);
                                                        HTML html = new HTML(eventObject.getText());
                                                        db2.setWidget(html);
                                                        db2.center();
                                                    }
                                                });
                                                map.addControl(wmsGetFeatureInfo);
                                                wmsGetFeatureInfo.activate();
                                                
                                                mapWidget.getElement().getFirstChildElement().getStyle().setZIndex(0); //force the map to fall behind popups
                                                panelForMap.setWidgetVerticalPosition(scrollPanel,Layout.Alignment.BEGIN);
                                                panelForMap.setWidgetLeftWidth(scrollPanel, 0, Unit.PCT, 30, Unit.PCT);
                                                panelForMap.setWidgetTopHeight(scrollPanel, 20, Unit.PCT, 100, Unit.PCT);
                                                
                                            }
                                        });
                                    } //end of get map if condition
                                    else if(requestName.equals("GetFeatureInfo"))
                                    {
                                        if(panelForWMS.remove(layerList))
                                        {
                                            panelForWMS.remove(crsList);
                                            panelForWMS.remove(stylesList);
                                            panelForWMS.remove(textBoxes);
                                            panelForWMS.remove(detailsLabel);
                                            panelForWMS.remove(boundingboxLabels);
                                            panelForWMS.remove(layersLabel);
                                            panelForWMS.remove(crsLabel);
                                            panelForWMS.remove(stylesLabel);
                                            panelForWMS.remove(submitButton);
                                        }
                                        
                                        final NodeList layers = messageDom.getElementsByTagName("Layer");
                                        int num = layers.getLength();
                                        for(i=1; i< num ; i++){
                                            
                                            NamedNodeMap attributes=layers.item(i).getAttributes();
                                            if(attributes.getNamedItem("queryable")!=null)
                                            {
                                                Node name = ((Element)layers.item(i)).getElementsByTagName("Name").item(0);
                                                String strname= name.getFirstChild().getNodeValue();
                                                featureInfoLayerList.addItem(strname);
                                            }
                                        }
                                        
                                        panelForWMS.add(supportedLayerLabel);
                                        panelForWMS.add(featureInfoLayerList);
                                    }
                                    else
                                    {
                                        if(panelForWMS.remove(layerList))
                                        {
                                            panelForWMS.remove(crsList);
                                            panelForWMS.remove(stylesList);
                                            panelForWMS.remove(textBoxes);
                                            panelForWMS.remove(detailsLabel);
                                            panelForWMS.remove(boundingboxLabels);
                                            panelForWMS.remove(layersLabel);
                                            panelForWMS.remove(crsLabel);
                                            panelForWMS.remove(stylesLabel);
                                            panelForWMS.remove(submitButton);
                                        }
                                        if(panelForWMS.remove(supportedLayerLabel))
                                        {
                                           panelForWMS.remove(featureInfoLayerList); 
                                        }
                                    }
                                }
                                
                            });
                            
                            //preparing the list for formats
                            NodeList formats=((Element)requests.item(0).getChildNodes().item(1)).getElementsByTagName("Format");
                            for(int i=0;i<formats.getLength();i++)
                            {
                                formatList.addItem(formats.item(i).getFirstChild().getNodeValue());
                            }
                            
                            panelForWMS.add(metadataLabel);
                            panelForWMS.add(new Label("Requests List:"));
                            panelForWMS.add(requestList);
                            panelForWMS.add(new Label("Formats Supported:"));
                            panelForWMS.add(formatList);
                            
                        } else {
                            Window.alert("Could not connect to server Error:"+response.getStatusCode());
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        // TODO Auto-generated method stub
                        Window.alert("Oh fax:"+exception.getMessage());
                    }
                });
            }
        catch(RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       return panelForWMS;
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
}
