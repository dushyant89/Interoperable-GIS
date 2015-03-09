package edu.iitb.csre.client;

import java.util.HashSet;

import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.protocol.WFSProtocol;
import org.gwtopenmaps.openlayers.client.protocol.WFSProtocolOptions;
import org.gwtopenmaps.openlayers.client.strategy.BBoxStrategy;
import org.gwtopenmaps.openlayers.client.strategy.Strategy;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/**
 * This class file separately handles the WFS use case
 * @author Bhaskar Bandyopadhyay github.com/bhaskarsuper9000
 *
 */
public class WFSManager {
	private Vector wfsLayer;
	//private HashSet<String> layerSet;
	private String currLayerNamespace;
	private String currLayerName;
	private static class RequestTypes{ public static final int 
		GET_CAPABILITIES = 0, DESCRIBE_FEATURE_TYPE = 1, GET_FEATURE = 2, 
		GET_GML_OBJECT = 3, LOCK_FEATURE = 4, GET_FEATURE_WITH_LOCK = 5,TRANSACTION = 6; }
	private static boolean firstTime = true;
	
	public Vector getWFSLayer() {

		if(firstTime == true){
			currLayerNamespace = "http://www.openplans.org/topp";
			currLayerName 	   = "states";
			//layerName = "ipcc-synthetic-vulnerability-climate-2005-2050-2100";
		}else{
			//layer name & namespace are set in click handler of button
			//probably nothing else to be done
		}

		//create wfs layer
		WFSProtocolOptions wfsProtocolOptions = new WFSProtocolOptions();
		
		//Get the Server URL from Main entry point class
		if(firstTime)
			wfsProtocolOptions.setUrl("http://localhost:8083/geoserver/wfs");
		else{
			String selectedServer=GNR629Project_1.server.get( GNR629Project_1.serverList.getItemText(GNR629Project_1.serverList.getSelectedIndex()) );
			wfsProtocolOptions.setUrl(selectedServer+"wfs");
		}
		
		wfsProtocolOptions.setFeatureType(currLayerName);
		wfsProtocolOptions.setFeatureNameSpace(currLayerNamespace);
		//wfsProtocolOptions.setFeatureNameSpace("http://sedac.ciesin.columbia.edu/gateway/guides/synthetic.html");

		WFSProtocol wfsProtocol = new WFSProtocol(wfsProtocolOptions);

		VectorOptions vectorOptions = new VectorOptions();
		vectorOptions.setProtocol(wfsProtocol);
		vectorOptions.setStrategies(new Strategy[]{new BBoxStrategy()});
		//if your wms is in a different projection, use entry pt projection
		vectorOptions.setProjection("EPSG:4326");

		wfsLayer = new Vector(currLayerName, vectorOptions);
		wfsLayer.setIsBaseLayer(false);
		wfsLayer.setIsVisible(true);
		wfsLayer.setOpacity(1);
		return wfsLayer;
	}

	public VerticalPanel getWFSPanel() {
		RequestBuilder builder;
		if(firstTime)
			builder = new RequestBuilder(RequestBuilder.GET, "http://localhost:8083/geoserver/wfs?request=getCapabilities");
		else{
			//String selectedServer=GNR629Project_1.serverList.getItemText(GNR629Project_1.serverList.getSelectedIndex());
			if( GNR629Project_1.serverList.getSelectedIndex() == 0 ){
				builder = new RequestBuilder(RequestBuilder.GET, "http://localhost:8083/geoserver/ows?service=wfs&version=1.1.0&request=GetCapabilities");
			}else
				builder = new RequestBuilder(RequestBuilder.GET, "http://sedac.ciesin.org/geoserver/ows?service=wfs&version=1.1.0&request=GetCapabilities");
		}
		//RequestBuilder builder=new RequestBuilder(RequestBuilder.GET, "http://sedac.ciesin.org/geoserver/ows?service=wfs&version=1.1.0&request=GetCapabilities");
		String requestData = null;
		//layerSet = new HashSet<String>();

		final VerticalPanel p = new VerticalPanel();
		final ListBox wfsReqList = new ListBox();

		final HTML metadataLabel = new HTML("<p style='border-bottom:1px solid black;'>Service Level  Metadata</p>");
		final HTML metadata = new HTML("");

		wfsReqList.addItem("Get Capabilities");
		wfsReqList.addItem("Describe Feature Type");
		wfsReqList.addItem("Get Feature");
		//wfsReqList.addItem("Get GML Object");
		//wfsReqList.addItem("Lock Feature");
		//wfsReqList.addItem("Get Feature With Lock");
		//wfsReqList.addItem("Transaction");

		final ListBox wfsFormatList_gc = new ListBox();
		final ListBox wfsFormatList_dft = new ListBox();
		final ListBox wfsFormatList_gf = new ListBox();
		//final ListBox wfsFormatList_ggo = new ListBox();
		final ListBox wfsLayerList  = new ListBox();

		final Button wfsAddLayerButton = new Button("Get Feature");

		try {
			builder.sendRequest(requestData, new RequestCallback() {

				public void onResponseReceived(Request request, Response response) {
					String xmlstr = null;
					if (200 == response.getStatusCode()) {
						xmlstr = response.getText();
						Document messageDom = XMLParser.parse(xmlstr);

						//Populate Metadata
						NodeList layers = messageDom.getElementsByTagName("ServiceIdentification");
						int num = layers.getLength();
						//Window.alert(Integer.toString(num));
						String data = "<p style='border-bottom:1px solid black;'>";
						for(int i=0; i< num ; i++){
							Node node = ((Element)layers.item(i));
							data += node.getNodeValue();
						}
						metadata.setHTML(data + "</p>");


						//Get the names of layers                            
						layers = messageDom.getElementsByTagName("FeatureType");
						num = layers.getLength();
						//int layerCount= num; // does not include outermost Layer tag element
						String result = "";

						for(int i=0; i< num ; i++){
							Node name = ((Element)layers.item(i)).getElementsByTagName("Name").item(0);
							String strname= name.getFirstChild().getNodeValue();

							result += strname;
							wfsLayerList.addItem(strname);
						}

						
						//Add the Get Capabilities format types to a wfsFormatList_gc
						HashSet<String> set = new HashSet<String>();
						layers = messageDom.getElementsByTagName("OperationsMetadata");
						num = layers.getLength();
						//Window.alert(Integer.toString(num) + layers.item(0).getFirstChild());
						//Window.alert(""+ ( (Element)layers.item(0)).getFirstChild().getNodeName() );
						//Node n = ( (Element)layers.item(0)).getFirstChild();
						layers = ( (Element)layers.item(0)).getChildNodes();
						num = layers.getLength();

						for(int i=0; i<num; i++){
							//Window.alert( ""+ layers.item(i).getNodeName() );
							if( ((Element)layers.item(i)).getAttribute("name").equals("GetCapabilities") ){
								NodeList n = ( (Element)layers.item(i) ).getElementsByTagName("Parameter");
								//Window.alert(""+n.getLength());
								for(int j=0; j<n.getLength(); j++){
									//Window.alert(""+  ((Element)n.item(j)).getAttribute("name") );
									if( ((Element)n.item(j)).getAttribute("name").equals("AcceptFormats") ){
										NodeList last = ((Element)n.item(j)).getElementsByTagName("Value");
										//Window.alert("zandu"+last.getLength());
										for( int k=0; k<last.getLength(); k++){
											//Window.alert( "" +((Node)last.item(k)).getFirstChild().getNodeValue() );
											set.add( ((Node)last.item(k)).getFirstChild().getNodeValue() );
										}
									}
								}
							}
						}
						//add output formats to ListBox
						for(String s: set){
							wfsFormatList_gc.addItem(s);
						}
						set.clear();
						
						
						//Add the DescribeFeatureType format types to a wfsFormatList_dft
						layers = messageDom.getElementsByTagName("OperationsMetadata");
						num = layers.getLength();
						layers = ( (Element)layers.item(0)).getChildNodes();
						num = layers.getLength();

						for(int i=0; i<num; i++){
							//Window.alert( ""+ layers.item(i).getNodeName() );
							if( ((Element)layers.item(i)).getAttribute("name").equals("DescribeFeatureType") ){
								NodeList n = ( (Element)layers.item(i) ).getElementsByTagName("Parameter");
								//Window.alert(""+n.getLength());
								for(int j=0; j<n.getLength(); j++){
									//Window.alert(""+  ((Element)n.item(j)).getAttribute("name") );
									if( ((Element)n.item(j)).getAttribute("name").equals("outputFormat") ){
										NodeList last = ((Element)n.item(j)).getElementsByTagName("Value");
										//Window.alert("zandu"+last.getLength());
										for( int k=0; k<last.getLength(); k++){
											//Window.alert( "" +((Node)last.item(k)).getFirstChild().getNodeValue() );
											set.add( ((Node)last.item(k)).getFirstChild().getNodeValue() );
										}
									}
								}
							}
						}
						//add output formats to ListBox
						for(String s: set){
							wfsFormatList_dft.addItem(s);
						}
						set.clear();
						
						
						//Add the Get Feature format types to a wfsFormatList_gf
						layers = messageDom.getElementsByTagName("OperationsMetadata");
						num = layers.getLength();
						//Node n = ( (Element)layers.item(0)).getFirstChild();
						layers = ( (Element)layers.item(0)).getChildNodes();
						num = layers.getLength();

						for(int i=0; i<num; i++){
							//Window.alert( ""+ layers.item(i).getNodeName() );
							if( ((Element)layers.item(i)).getAttribute("name").equals("GetFeature") ){
								NodeList n = ( (Element)layers.item(i) ).getElementsByTagName("Parameter");
								//Window.alert(""+n.getLength());
								for(int j=0; j<n.getLength(); j++){
									//Window.alert(""+  ((Element)n.item(j)).getAttribute("name") );
									if( ((Element)n.item(j)).getAttribute("name").equals("outputFormat") ){
										NodeList last = ((Element)n.item(j)).getElementsByTagName("Value");
										//Window.alert("zandu"+last.getLength());
										for( int k=0; k<last.getLength(); k++){
											//Window.alert( "" +((Node)last.item(k)).getFirstChild().getNodeValue() );
											set.add( ((Node)last.item(k)).getFirstChild().getNodeValue() );
										}
									}
								}
							}
						}
						//add output formats to ListBox
						for(String s: set){
							wfsFormatList_gf.addItem(s);
						}
						set.clear();
						
						
						
						
						//Window.alert("Hey :"+result);

					} else {
						Window.alert("Could not connect to server Error:"+response.getStatusCode());
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					Window.alert(exception.getMessage());
				}
			});
		}
		catch(RequestException e) {
			Window.alert(e.getMessage());
		}
		catch(DOMException e){
			Window.alert(e.getMessage());
		}

		//Add event handlers for the ListBoxes
		wfsReqList.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				

				switch( ((ListBox)event.getSource()).getSelectedIndex() ){
				case RequestTypes.GET_CAPABILITIES:
					p.clear();
					p.add(metadataLabel);
					//p.add(metadata);
					p.add(new Label("Requests List:"));
					p.add(wfsReqList);
					p.add(new Label("Formats Supported:"));
					p.add(wfsFormatList_gc);
					break;
				case RequestTypes.DESCRIBE_FEATURE_TYPE:
					p.clear();
					p.add(metadataLabel);
					//p.add(metadata);
					p.add(new Label("Requests List:"));
					p.add(wfsReqList);
					p.add(new Label("Formats Supported:"));
					p.add(wfsFormatList_dft);
					break;
				case RequestTypes.GET_FEATURE:
					p.clear();
					p.add(metadataLabel);
					p.add(new Label("Requests List:"));
					p.add(wfsReqList);
					p.add(new Label("Formats Supported:"));
					p.add(wfsFormatList_gf);
					p.add(new Label("Layers List:"));
					p.add(wfsLayerList);
					p.add(new HTML("<p></p>"));
					p.add(wfsAddLayerButton);
					break;

					//				case RequestTypes.GET_GML_OBJECT:
					//					break;
					//				case RequestTypes.LOCK_FEATURE:
					//					break;
					//				case RequestTypes.GET_FEATURE_WITH_LOCK:
					//					break;
					//				case RequestTypes.TRANSACTION:
					//					break;
					//				
				default:
					Window.alert("This request type is not yet supported");
				}
			}
		});

		wfsAddLayerButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				//Window.alert(layerSet.toString());
				//layerSet.add(wfsLayerList.getItemText(wfsLayerList.getSelectedIndex()));
				String str = wfsLayerList.getItemText(wfsLayerList.getSelectedIndex());
				//for(String s : layerSet){
					//str += s.substring(s.indexOf(":")+1)+",";
				//}
				//str = str.substring(0,str.length()-1);
				currLayerName = str.substring(str.indexOf(":")+1);
				currLayerNamespace = str.substring(0, str.indexOf(":"));
				//Window.alert(currLayerName + ":" + currLayerNamespace + str);
				                                    
				GNR629Project_1.map.addLayer(getWFSLayer());
				
			}
		});

		//default UI (get capabilities)
		p.add(metadataLabel);
		//p.add(metadata);
		p.add(new Label("Requests List:"));
		p.add(wfsReqList);
		p.add(new Label("Formats Supported:"));
		p.add(wfsFormatList_gc);
		//p.add(new Label("Layers List:"));
		//p.add(wfsLayerList);

		//maybe this is the right place
		firstTime = false;
		return p;
	}


}
